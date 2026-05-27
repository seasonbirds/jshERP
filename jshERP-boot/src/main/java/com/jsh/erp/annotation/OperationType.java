package com.jsh.erp.annotation;

/**
 * 审计日志操作类型枚举
 */
public enum OperationType {
    ADD("新增"),
    BATCH_ADD("批量新增"),
    EDIT("修改"),
    DELETE("删除"),
    LOGIN("登录"),
    IMPORT("导入"),
    ENABLED("更新状态"),
    CUSTOM("");

    private final String prefix;

    OperationType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
