package com.jsh.erp.aspect;

import com.jsh.erp.annotation.LogContentProvider;
import com.jsh.erp.annotation.LogUserContext;
import com.jsh.erp.annotation.OperationLog;
import com.jsh.erp.annotation.OperationType;
import com.jsh.erp.datasource.entities.Log;
import com.jsh.erp.datasource.mappers.LogMapper;
import com.jsh.erp.datasource.mappers.LogMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.service.RedisService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.SensitiveDataMasker;
import com.jsh.erp.utils.Tools;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Aspect
@Component
public class OperationLogAspect {
    private Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Resource
    private LogMapper logMapper;

    @Resource
    private LogMapperEx logMapperEx;

    @Resource
    private UserService userService;

    @Resource
    private RedisService redisService;

    private ExpressionParser parser = new SpelExpressionParser();

    @AfterReturning(value = "@annotation(operationLog)", returning = "returnValue")
    public void recordLog(JoinPoint joinPoint, OperationLog operationLog, Object returnValue) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return;
            }

            Long userId;
            Long tenantId = null;

            if (operationLog.useExplicitUser()) {
                userId = LogUserContext.getUserId();
                tenantId = LogUserContext.getTenantId();
                if (userId == null) {
                    userId = userService.getUserId(request);
                    tenantId = Tools.getTenantIdByToken(request.getHeader("X-Access-Token"));
                }
            } else {
                userId = userService.getUserId(request);
            }

            if (userId == null) {
                return;
            }

            String clientIp = Tools.getLocalIp(request);
            String moduleName = operationLog.moduleName();
            String providerModuleName = LogContentProvider.getModuleName();
            if (providerModuleName != null && !providerModuleName.isEmpty()) {
                moduleName = providerModuleName;
            }

            if (!operationLog.useExplicitUser()) {
                String createTime = Tools.getNow3();
                Long count = logMapperEx.getCountByIpAndDate(userId, moduleName, clientIp, createTime);
                if (count > 0) {
                    redisService.deleteObjectByUserAndIp(userId, clientIp);
                    return;
                }
            }

            String content = resolveContent(joinPoint, operationLog, returnValue);
            content = SensitiveDataMasker.mask(content);

            Log log = new Log();
            log.setUserId(userId);
            log.setOperation(moduleName);
            log.setClientIp(clientIp);
            log.setCreateTime(new Date());
            log.setStatus((byte) 0);
            log.setContent(content);

            if (operationLog.useExplicitUser()) {
                log.setTenantId(tenantId);
                logMapperEx.insertLogWithUserId(log);
            } else {
                logMapper.insertSelective(log);
            }
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        } finally {
            LogContentProvider.clear();
            LogUserContext.clear();
        }
    }

    private String resolveContent(JoinPoint joinPoint, OperationLog operationLog, Object returnValue) {
        String providerContent = LogContentProvider.getContent();
        if (providerContent != null && !providerContent.isEmpty()) {
            return providerContent;
        }

        String spel = operationLog.content();
        String operationPrefix = operationLog.operationType().getValue();

        if (spel.isEmpty()) {
            return operationPrefix;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        if (returnValue != null) {
            context.setVariable("result", returnValue);
        }

        try {
            Expression expression = parser.parseExpression(spel);
            Object value = expression.getValue(context);
            String dynamicContent = value != null ? value.toString() : "";
            return operationPrefix + dynamicContent;
        } catch (Exception e) {
            logger.warn("SpEL expression evaluation failed: {}", spel, e);
            return operationPrefix;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}