package com.jsh.erp.annotation;

public class LogContentProvider {
    private static final ThreadLocal<String> CONTENT_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> MODULE_NAME_HOLDER = new ThreadLocal<>();

    public static void setContent(String content) {
        CONTENT_HOLDER.set(content);
    }

    public static String getContent() {
        String content = CONTENT_HOLDER.get();
        CONTENT_HOLDER.remove();
        return content;
    }

    public static void setModuleName(String moduleName) {
        MODULE_NAME_HOLDER.set(moduleName);
    }

    public static String getModuleName() {
        String moduleName = MODULE_NAME_HOLDER.get();
        MODULE_NAME_HOLDER.remove();
        return moduleName;
    }

    public static void clear() {
        CONTENT_HOLDER.remove();
        MODULE_NAME_HOLDER.remove();
    }
}