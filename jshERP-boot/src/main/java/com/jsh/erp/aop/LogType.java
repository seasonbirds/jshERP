package com.jsh.erp.aop;

/**
 * 审计日志操作类型枚举
 */
public enum LogType {
    /** 新增 */
    ADD("新增"),
    /** 批量新增 */
    BATCH_ADD("批量新增"),
    /** 修改 */
    EDIT("修改"),
    /** 删除 */
    DELETE("删除"),
    /** 登录 */
    LOGIN("登录"),
    /** 导入 */
    IMPORT("导入"),
    /** 更新状态 */
    ENABLED("更新状态"),
    /** 其它操作 */
    OTHER("其它");

    private final String value;

    LogType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
