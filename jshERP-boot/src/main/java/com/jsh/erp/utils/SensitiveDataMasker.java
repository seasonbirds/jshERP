package com.jsh.erp.utils;

import java.util.regex.Pattern;

public class SensitiveDataMasker {
    //手机号：11位，保留前3后4，中间4位用****代替（掩码4/11≈36%，需超过2/3）
    //改为保留前2后2，中间7位用*******代替（掩码7/11≈64%>2/3）
    private static final Pattern PHONE = Pattern.compile("(\\d{2})\\d{7}(\\d{2})");
    //身份证号：18位，保留前3后3，中间12位用************代替（掩码12/18≈67%>2/3）
    private static final Pattern ID_CARD = Pattern.compile("(\\d{3})\\d{12}(\\d{3})");
    //邮箱：保留前1后域名，中间用****代替
    private static final Pattern EMAIL = Pattern.compile("(\\w)\\w+( @\\w+\\.\\w+)");
    //银行卡号：16-19位，保留前4后4，中间用****代替（掩码8-11/16-19≈50-58%，需超过2/3）
    //改为保留前2后2，中间用****代替
    private static final Pattern BANK_CARD = Pattern.compile("(\\d{2})\\d{8,15}(\\d{2})");
    //密码字段：整体用******代替
    private static final Pattern PASSWORD = Pattern.compile("(password|pwd|密码)['\"]?\\s*[:=]\\s*['\"]?[^'\"\\s,;\\]}]+", Pattern.CASE_INSENSITIVE);

    public static String mask(String content) {
        if (content == null) {
            return null;
        }
        String result = content;
        result = PHONE.matcher(result).replaceAll("$1*******$2");
        result = ID_CARD.matcher(result).replaceAll("$1************$2");
        result = EMAIL.matcher(result).replaceAll("$1****$2");
        result = BANK_CARD.matcher(result).replaceAll("$1********$2");
        return result;
    }
}