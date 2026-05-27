package com.jsh.erp.annotation;

public class LogUserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void set(Long userId, Long tenantId) {
        USER_ID.set(userId);
        TENANT_ID.set(tenantId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
        TENANT_ID.remove();
    }
}