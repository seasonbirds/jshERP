package com.jsh.erp.annotation;

/**
 * 审计日志ThreadLocal上下文持有者。
 * 用于在Service方法内部向 {@link AuditLogAspect} 切面传递动态日志内容。
 *
 * <p>使用场景：
 * <ul>
 *   <li>{@link #setContentDetail(String)} — 设置内容详情部分，与操作类型前缀拼接。
 *       例如：operation=ADD时，setContentDetail("admin") → content="新增admin"</li>
 *   <li>{@link #setFullContent(String)} — 设置完整内容，忽略操作类型前缀。
 *       例如：setFullContent("强制结单：DH001") → content="强制结单：DH001"</li>
 *   <li>{@link #setModuleName(String)} — 动态覆盖注解中声明的模块名。
 *       适用于模块名运行时确定的场景（如导入操作的type参数）</li>
 *   <li>{@link #skipLog()} — 跳过本次日志记录。适用于条件不满足时无需记录日志</li>
 *   <li>{@link #setUserId(Long)} / {@link #setTenantId(Long)} — 配合useExplicitUserId=true使用</li>
 * </ul>
 *
 * <p>所有ThreadLocal均在切面的finally块中清除，不会泄漏。
 */
public class AuditContextHolder {

    private static final ThreadLocal<String> CONTENT_DETAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> FULL_CONTENT = new ThreadLocal<>();
    private static final ThreadLocal<String> MODULE_NAME = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SKIP_LOG = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 设置日志内容的详情部分，会与操作类型前缀拼接。
     * 例如：operation=ADD, setContentDetail("admin") → "新增admin"
     */
    public static void setContentDetail(String detail) {
        CONTENT_DETAIL.set(detail);
    }

    /**
     * 设置完整的日志内容，覆盖操作类型前缀。
     * 例如：setFullContent("强制结单：DH001") → content="强制结单：DH001"
     */
    public static void setFullContent(String content) {
        FULL_CONTENT.set(content);
    }

    /**
     * 动态覆盖注解中声明的模块名。
     * 适用于模块名运行时确定的场景（如SupplierService.importExcel）。
     */
    public static void setModuleName(String moduleName) {
        MODULE_NAME.set(moduleName);
    }

    /**
     * 跳过本次日志记录。用于条件不满足时无需记录日志的场景。
     */
    public static void skipLog() {
        SKIP_LOG.set(Boolean.TRUE);
    }

    /**
     * 设置显式userId，配合 @AuditLog(useExplicitUserId=true) 使用。
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 设置显式tenantId，配合 @AuditLog(useExplicitUserId=true) 使用。
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    // ========== 切面读取方法 ==========

    public static String getContentDetail() {
        return CONTENT_DETAIL.get();
    }

    public static String getFullContent() {
        return FULL_CONTENT.get();
    }

    public static String getModuleName() {
        return MODULE_NAME.get();
    }

    public static boolean isSkipLog() {
        Boolean skip = SKIP_LOG.get();
        return skip != null && skip;
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除所有ThreadLocal状态。必须在切面的finally块中调用。
     */
    public static void clear() {
        CONTENT_DETAIL.remove();
        FULL_CONTENT.remove();
        MODULE_NAME.remove();
        SKIP_LOG.remove();
        USER_ID.remove();
        TENANT_ID.remove();
    }
}
