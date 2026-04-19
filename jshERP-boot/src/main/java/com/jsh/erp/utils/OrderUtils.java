package com.jsh.erp.utils;

import java.util.regex.Pattern;

/**
 * @author jishenghua qq752718920  2018-10-7 15:26:27
 */
public class OrderUtils {

    private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private static boolean isValidColumnName(String column) {
        if (StringUtil.isEmpty(column)) {
            return false;
        }
        return COLUMN_NAME_PATTERN.matcher(column).matches();
    }

    private static boolean isValidTableName(String table) {
        if (StringUtil.isEmpty(table)) {
            return false;
        }
        return TABLE_NAME_PATTERN.matcher(table).matches();
    }

    private static boolean isValidOrderDirection(String direction) {
        if (StringUtil.isEmpty(direction)) {
            return false;
        }
        String upperDir = direction.trim().toUpperCase();
        return "ASC".equals(upperDir) || "DESC".equals(upperDir);
    }

    /**
     * 将指定字段排序
     *
     * @param orders 格式 属性名,排序方式 例如( name,asc或ip,desc)
     * @return 排序字符串 例如：（name asc 或 ip desc）
     */
    public static String getOrderString(String orders) {
        if (StringUtil.isNotEmpty(orders)) {
            String[] splits = orders.split(Constants.SPLIT);
            if (splits.length == 2) {
                String column = ColumnPropertyUtil.propertyToColumn(splits[0]);
                String direction = splits[1];
                if (!isValidColumnName(column) || !isValidOrderDirection(direction)) {
                    return "";
                }
                if (column.equals("audit_status")) {
                    return "IF(`audit_status`=3,-1,`audit_status`) " + direction.toUpperCase();
                } else if (column.equals("create_time") || column.equals("modify_time")) {
                    return column + " " + direction.toUpperCase();
                } else {
                    return "convert(" + column + " using gbk) " + direction.toUpperCase();
                }
            }
        }
        return "";
    }

    public static String getJoinTablesOrderString(String orders, String tableName) {
        if (StringUtil.isNotEmpty(orders)) {
            String[] splits = orders.split(Constants.SPLIT);
            if (splits.length == 2) {
                String column = ColumnPropertyUtil.propertyToColumn(splits[0]);
                String direction = splits[1];
                if (!isValidColumnName(column) || !isValidOrderDirection(direction) || !isValidTableName(tableName)) {
                    return "";
                }
                return "convert(" + tableName + "." + column + " using gbk) " + direction.toUpperCase();
            }
        }
        return "";
    }


    /**
     * 将指定字段排序
     * inet_aton：mysql将IP 转成 long类别函数
     *
     * @param orders         格式 属性名,排序方式 例如( name,asc或ip,desc)
     * @param ipPropertyName 如果需要按IP属性排序，需要将属性名传入（可不传）
     * @return 排序字符串 例如：（name asc 或 ip desc）
     */
    public static String getOrderString(String orders, String... ipPropertyName) {
        if (StringUtil.isNotEmpty(orders)) {
            String[] splits = orders.split(Constants.SPLIT);
            if (splits.length == 2) {
                String column = ColumnPropertyUtil.propertyToColumn(splits[0]);
                String direction = splits[1];
                if (!isValidColumnName(column) || !isValidOrderDirection(direction)) {
                    return "";
                }
                if (ipPropertyName != null && ipPropertyName.length > 0) {
                    for (String ip : ipPropertyName) {
                        if (ip.equals(column)) {
                            return "inet_aton(" + column + ") " + direction.toUpperCase();
                        }
                    }
                }
                return column + " " + direction.toUpperCase();
            }
        }
        return "";
    }
}
