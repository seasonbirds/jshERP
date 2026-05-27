package com.jsh.erp.annotation;

import com.jsh.erp.service.LogService;
import com.jsh.erp.utils.PrivacyDataMasker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 审计日志AOP切面。
 *
 * <p>拦截所有标注了 {@link AuditLog} 注解的Service方法，
 * 在方法执行成功后自动记录审计日志到 jsh_log 表。
 *
 * <p>{@code @Order(2)} 确保切面在 {@code @Transactional}（默认Order= Integer.MAX_VALUE）外层执行，
 * 即事务提交成功后才写入日志，避免操作失败时产生虚假审计记录。
 *
 * <p>自动从请求上下文获取：
 * <ul>
 *   <li>userId — 通过 X-Access-Token 从Redis获取</li>
 *   <li>tenantId — 从token字符串中解析</li>
 *   <li>clientIp — 通过 Tools.getLocalIp 获取</li>
 * </ul>
 */
@Aspect
@Component
@Order(2)
public class AuditLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);

    @Resource
    private LogService logService;

    @Around("@annotation(com.jsh.erp.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 执行前清除ThreadLocal，防止残留数据
        AuditContextHolder.clear();
        try {
            // 执行目标方法（包括事务提交）
            Object result = pjp.proceed();

            // 如果方法显式跳过日志，则不记录
            if (AuditContextHolder.isSkipLog()) {
                return result;
            }

            // 获取注解
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            AuditLog auditLog = method.getAnnotation(AuditLog.class);
            if (auditLog == null) {
                // 接口方法上的注解，需要从实现类获取
                try {
                    method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
                    auditLog = method.getAnnotation(AuditLog.class);
                } catch (Exception ignored) {
                    // 找不到则跳过
                }
                if (auditLog == null) {
                    return result;
                }
            }

            // 写入日志，失败不影响业务
            try {
                writeLog(auditLog, pjp);
            } catch (Exception e) {
                logger.error("审计日志写入失败: " + e.getMessage(), e);
            }

            return result;
        } catch (Throwable t) {
            // 方法执行失败（异常抛出），不记录日志
            throw t;
        } finally {
            // 确保清除ThreadLocal
            AuditContextHolder.clear();
        }
    }

    private void writeLog(AuditLog auditLog, ProceedingJoinPoint pjp) throws Exception {
        // 确定模块名：ThreadLocal覆盖注解值
        String module = AuditContextHolder.getModuleName();
        if (module == null || module.isEmpty()) {
            module = auditLog.module();
        }
        if (module == null || module.isEmpty()) {
            return;
        }

        // 构建日志内容
        String content = buildContent(auditLog);
        if (content == null || content.isEmpty()) {
            return;
        }

        // 隐私数据掩码
        content = PrivacyDataMasker.mask(content);

        // 获取HttpServletRequest
        HttpServletRequest request = extractRequest(pjp);
        if (request == null) {
            logger.warn("无法获取HttpServletRequest，跳过审计日志记录");
            return;
        }

        if (auditLog.useExplicitUserId()) {
            // 显式userId路径：登录、系统配置等场景
            Long userId = AuditContextHolder.getUserId();
            Long tenantId = AuditContextHolder.getTenantId();
            if (userId != null) {
                logService.insertLogWithUserId(userId, tenantId, module, content, request);
            }
        } else {
            // 标准路径：从request中提取userId
            logService.insertLog(module, content, request);
        }
    }

    /**
     * 构建日志内容字符串。
     * 优先级：fullContent > prefix + detail > prefix
     */
    private String buildContent(AuditLog auditLog) {
        // 优先级1：完整内容覆盖（忽略操作前缀）
        String fullContent = AuditContextHolder.getFullContent();
        if (fullContent != null) {
            return fullContent;
        }

        // 优先级2：操作前缀 + 详情
        String prefix = auditLog.operation().getPrefix();
        String detail = AuditContextHolder.getContentDetail();
        if (detail != null && !detail.isEmpty()) {
            return prefix + detail;
        }

        // 优先级3：仅操作前缀（如 ENABLED → "更新状态"）
        if (!prefix.isEmpty()) {
            return prefix;
        }

        // CUSTOM类型且未设置内容 → 跳过
        return null;
    }

    /**
     * 提取HttpServletRequest。优先从方法参数中获取，其次从RequestContextHolder获取。
     */
    private HttpServletRequest extractRequest(ProceedingJoinPoint pjp) {
        // 先扫描方法参数
        Object[] args = pjp.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        // 回退到RequestContextHolder
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
