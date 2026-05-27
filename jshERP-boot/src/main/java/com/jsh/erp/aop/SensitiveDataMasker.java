package com.jsh.erp.aop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 * 对密码、身份证号、手机号码、邮箱地址、银行账号等进行掩码处理。
 * 掩码规则：关键部分用*代替，代替内容占2/3以上。
 */
public class SensitiveDataMasker {

    /** 中国大陆手机号 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<![0-9])1[3-9][0-9]{9}(?![0-9])");
    /** 中国大陆身份证号 (18位/15位) */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(?<![0-9X])([1-9][0-9]{5}(19|20)[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[0-9]{3}[0-9Xx]|(?<![0-9])[1-9][0-9]{5}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[0-9]{3}(?![0-9Xx]))");
    /** 邮箱地址 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?<![A-Za-z0-9._%+-])[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(?![A-Za-z])");
    /** 银行卡号 (16-19位数字) */
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(?<![0-9])[0-9]{16,19}(?![0-9])");
    /** JSON/表单字段名中包含密码类关键字 */
    private static final Pattern PASSWORD_FIELD_PATTERN = Pattern.compile(
            "(password|pwd|密码|secret|token|credentials)(\"|')?\\s*[:=]\\s*(\"|')?([^\"',}\\s]+)",
            Pattern.CASE_INSENSITIVE);

    private SensitiveDataMasker() {
    }

    /**
     * 对日志内容进行敏感数据脱敏
     * @param content 原始日志内容
     * @return 脱敏后的内容
     */
    public static String mask(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        content = maskPhone(content);
        content = maskIdCard(content);
        content = maskEmail(content);
        content = maskBankCard(content);
        content = maskPasswordField(content);
        return content;
    }

    /**
     * 手机号脱敏：保留前3位，其余用*代替（11位号码中8位用*代替，占73%）
     */
    private static String maskPhone(String content) {
        Matcher matcher = PHONE_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String phone = matcher.group();
            String masked = phone.substring(0, 3) + repeat("*", phone.length() - 3);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 身份证号脱敏：保留前3位，其余用*代替（18位中15位用*，占83%）
     */
    private static String maskIdCard(String content) {
        Matcher matcher = ID_CARD_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String idCard = matcher.group();
            if (idCard != null && idCard.length() >= 15) {
                String masked = idCard.substring(0, 3) + repeat("*", idCard.length() - 3);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 邮箱脱敏：用户名保留首字符，其余用*代替，域名保留首尾字符
     */
    private static String maskEmail(String content) {
        Matcher matcher = EMAIL_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String email = matcher.group();
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                String localPart = email.substring(0, atIndex);
                String domainPart = email.substring(atIndex); // includes @
                String maskedLocal;
                if (localPart.length() <= 2) {
                    maskedLocal = localPart.charAt(0) + repeat("*", Math.max(localPart.length() - 1, 2));
                } else {
                    maskedLocal = localPart.charAt(0) + repeat("*", localPart.length() - 1);
                }
                String maskedDomain;
                if (domainPart.length() <= 4) {
                    maskedDomain = domainPart;
                } else {
                    // 保留@和域名首字符，其余用*代替，最后保留顶级域名
                    int dotIndex = domainPart.lastIndexOf('.');
                    if (dotIndex > 1) {
                        String domainName = domainPart.substring(1, dotIndex); // without @
                        String tld = domainPart.substring(dotIndex); // .com etc.
                        if (domainName.length() <= 2) {
                            maskedDomain = "@" + domainName + repeat("*", Math.max(0, tld.length() - 2)) + tld.substring(Math.max(0, tld.length() - 2));
                        } else {
                            maskedDomain = "@" + domainName.charAt(0) + repeat("*", domainName.length() - 1) + tld;
                        }
                    } else {
                        maskedDomain = "@" + repeat("*", domainPart.length() - 1);
                    }
                }
                String masked = maskedLocal + maskedDomain;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 银行账号脱敏：保留前4位和后2位，中间用*代替
     */
    private static String maskBankCard(String content) {
        Matcher matcher = BANK_CARD_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String card = matcher.group();
            if (card.length() >= 8) {
                int maskLen = card.length() - 6;
                String masked = card.substring(0, 4) + repeat("*", maskLen) + card.substring(card.length() - 2);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 对JSON/表单中密码类字段值进行脱敏
     */
    private static String maskPasswordField(String content) {
        Matcher matcher = PASSWORD_FIELD_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String quoteChar2 = matcher.group(2) != null ? matcher.group(2) : "";
            String separator = matcher.group(0).substring(fieldName.length() + quoteChar2.length());
            // 分离出 [:或= 和 引号] 以及实际值
            int eqIdx = separator.indexOf('=');
            int colonIdx = separator.indexOf(':');
            String sep = eqIdx >= 0 ? separator.substring(0, eqIdx + 1) : separator.substring(0, colonIdx + 1);
            String valuePart = separator.substring(sep.length());
            String quote = "";
            String value = valuePart;
            if (valuePart.startsWith("\"") || valuePart.startsWith("'")) {
                quote = valuePart.substring(0, 1);
                value = valuePart.substring(1);
            }
            String maskedValue = repeat("*", Math.max(value.length(), 6));
            String replacement = fieldName + quoteChar2 + sep + quote + maskedValue;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 对方法参数进行脱敏（用于logParams=true时）
     * 将参数转为字符串后对敏感信息进行脱敏
     */
    public static String maskParams(String paramString) {
        if (paramString == null || paramString.isEmpty()) {
            return paramString;
        }
        return mask(paramString);
    }

    private static String repeat(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
