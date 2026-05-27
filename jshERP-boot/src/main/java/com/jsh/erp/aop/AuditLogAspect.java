package com.jsh.erp.aop;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.annotation.AuditLog;
import com.jsh.erp.service.LogService;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.SensitiveDataUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
public class AuditLogAspect {

    private Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);

    @Resource
    private LogService logService;

    @Resource
    private UserService userService;

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return;
            }
            String moduleName = auditLog.moduleName();
            String content = buildContent(joinPoint, auditLog, result);
            content = SensitiveDataUtil.maskSensitiveData(content);
            Long userId = null;
            try {
                userId = userService.getUserId(request);
            } catch (Exception e) {
                //ignore
            }
            if (userId != null) {
                logService.insertLog(moduleName, content, request);
            } else {
                Long loginUserId = getParamByName(joinPoint, "auditUserId", Long.class);
                Long tenantId = getParamByName(joinPoint, "auditTenantId", Long.class);
                if (loginUserId != null) {
                    logService.insertLogWithUserId(loginUserId, tenantId, moduleName, content, request);
                }
            }
        } catch (Exception e) {
            logger.error("审计日志记录失败", e);
        } finally {
            AuditLogContext.clear();
        }
    }

    private String buildContent(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        String threadLocalContent = AuditLogContext.getContent();
        if (threadLocalContent != null && !threadLocalContent.isEmpty()) {
            return threadLocalContent;
        }
        String expression = auditLog.contentExpression();
        if (expression != null && !expression.isEmpty()) {
            return resolveExpression(expression, joinPoint, result);
        }
        StringBuilder sb = new StringBuilder();
        if (!auditLog.operationType().isEmpty()) {
            sb.append(auditLog.operationType());
        }
        if (auditLog.logParams()) {
            String paramContent = getParamByName(joinPoint, "auditContent", String.class);
            if (paramContent != null) {
                sb.append(paramContent);
            }
        }
        if (auditLog.logResult() && result != null) {
            sb.append(result.toString());
        }
        return sb.toString();
    }

    private String resolveExpression(String expression, JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        String resolved = expression;
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] != null) {
                    String placeholder = "#{" + paramNames[i] + "}";
                    if (resolved.contains(placeholder)) {
                        resolved = resolved.replace(placeholder, args[i].toString());
                    }
                    if (args[i] instanceof JSONObject) {
                        JSONObject jsonObj = (JSONObject) args[i];
                        for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
                            String jsonPlaceholder = "#{" + paramNames[i] + "." + entry.getKey() + "}";
                            if (resolved.contains(jsonPlaceholder) && entry.getValue() != null) {
                                resolved = resolved.replace(jsonPlaceholder, entry.getValue().toString());
                            }
                        }
                    } else {
                        try {
                            Method[] methods = args[i].getClass().getMethods();
                            for (Method m : methods) {
                                if (m.getName().startsWith("get") && m.getParameterCount() == 0
                                        && !m.getName().equals("getClass")) {
                                    String fieldName = m.getName().substring(3);
                                    fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                                    String objPlaceholder = "#{" + paramNames[i] + "." + fieldName + "}";
                                    if (resolved.contains(objPlaceholder)) {
                                        Object val = m.invoke(args[i]);
                                        resolved = resolved.replace(objPlaceholder, val != null ? val.toString() : "");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            //ignore reflection errors
                        }
                    }
                }
            }
        }
        if (result != null) {
            resolved = resolved.replace("#{result}", result.toString());
        }
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private <T> T getParamByName(JoinPoint joinPoint, String name, Class<T> type) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (name.equals(paramNames[i]) && args[i] != null && type.isInstance(args[i])) {
                    return type.cast(args[i]);
                }
            }
        }
        return null;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }
}
