package com.jsh.erp.aop;

public class AuditLogContext {

    private static final ThreadLocal<String> CONTENT_HOLDER = new ThreadLocal<>();

    private AuditLogContext() {
    }

    public static void setContent(String content) {
        CONTENT_HOLDER.set(content);
    }

    public static String getContent() {
        return CONTENT_HOLDER.get();
    }

    public static void clear() {
        CONTENT_HOLDER.remove();
    }
}
