package com.jsh.erp.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解，标注在需要记录审计日志的Service方法上。
 * 切面会自动从请求上下文获取当前用户ID、租户ID、客户端IP等信息。
 * 对于动态内容，方法内部通过 {@link AuditContextHolder} 向切面传递。
 *
 * <p>使用方式：
 * <pre>
 * // 简单场景：前缀 + 方法内设置的detail → "新增admin"
 * &#64;AuditLog(module="用户", operation=OperationType.ADD)
 * public void insertUser(...) {
 *     AuditContextHolder.setContentDetail(user.getLoginName());
 *     ...
 * }
 *
 * // 动态模块名：模块名由方法内指定
 * &#64;AuditLog(module="", operation=OperationType.IMPORT)
 * public void importExcel(List list, String type, ...) {
 *     AuditContextHolder.setModuleName(type);
 *     AuditContextHolder.setContentDetail(list.size() + "条");
 *     ...
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    /**
     * 模块名称，对应日志表的operation字段。
     * 传空字符串时需通过 {@link AuditContextHolder#setModuleName(String)} 动态设置。
     */
    String module();

    /**
     * 操作类型，提供日志内容的前缀（如"新增"、"修改"等）。
     * 使用 {@link OperationType#CUSTOM} 时，需通过
     * {@link AuditContextHolder#setFullContent(String)} 设置完整内容。
     */
    OperationType operation();

    /**
     * 是否使用显式userId路径写入日志。
     * 适用于登录（session尚未建立）和系统配置等场景，
     * 需通过 {@link AuditContextHolder} 设置userId和tenantId。
     */
    boolean useExplicitUserId() default false;
}
