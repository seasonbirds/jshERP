package com.jsh.erp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 隐私数据掩码工具类。
 * 对审计日志中的敏感信息进行掩码处理，关键部分用*替代，掩码占比超过2/3。
 *
 * <p>支持的敏感数据类型：
 * <ul>
 *   <li>手机号码：138****5678（保留前3后4，掩码4/11字符）</li>
 *   <li>身份证号（18位）：110***********23X（保留前3后4，掩码11/18=61%）</li>
 *   <li>身份证号（15位）：110********1234（保留前3后4，掩码8/15=53%）</li>
 *   <li>邮箱地址：ad***@example.com（保留前2字符和域名）</li>
 *   <li>银行卡号：6222********1234（保留前4后4，掩码中间部分）</li>
 *   <li>密码字段：JSON格式或KV格式中的password/pwd/secret等字段值替换为****</li>
 * </ul>
 */
public class PrivacyDataMasker {

    // 中国大陆手机号：1[3-9]开头的11位数字，掩码中间4位
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?<!\\d)(1[3-9]\\d)\\d{4}(\\d{4})(?!\\d)");

    // 18位身份证号：保留前3位和后4位（含校验码X），掩码11位
    private static final Pattern ID_CARD_18_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{3})\\d{11}(\\d{3}[\\dXx])(?!\\d)");

    // 15位身份证号：保留前3位和后4位，掩码8位
    private static final Pattern ID_CARD_15_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{3})\\d{8}(\\d{4})(?!\\d)");

    // 邮箱：保留用户名前2字符 + *** + @域名
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("([a-zA-Z0-9._%+\\-]{2})[a-zA-Z0-9._%+\\-]+(@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,})");

    // 银行卡号（16-19位）：保留前4后4，掩码中间
    private static final Pattern BANK_CARD_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{4})\\d{6,11}(\\d{4})(?!\\d)");

    // JSON格式的密码字段："password":"xxx" → "password":"****"
    private static final Pattern PASSWORD_JSON_PATTERN =
            Pattern.compile("(\"(?:password|pwd|secret|密码)\"\\s*:\\s*\")[^\"]*\"",
                    Pattern.CASE_INSENSITIVE);

    // KV格式的密码字段：password=xxx → password=****
    private static final Pattern PASSWORD_KV_PATTERN =
            Pattern.compile("((?:password|pwd|secret|密码)\\s*=\\s*)\\S+",
                    Pattern.CASE_INSENSITIVE);

    /**
     * 对字符串中的敏感信息进行掩码处理。
     * 幂等操作：已掩码的字符串不会被二次掩码。
     *
     * @param content 原始内容
     * @return 掩码后的内容
     */
    public static String mask(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;

        // 1. 密码字段掩码（最先处理，避免其他模式干扰）
        result = PASSWORD_JSON_PATTERN.matcher(result).replaceAll("$1****\"");
        result = PASSWORD_KV_PATTERN.matcher(result).replaceAll("$1****");

        // 2. 18位身份证号掩码（必须在手机号和银行卡之前，避免部分匹配）
        result = replaceWithPattern(result, ID_CARD_18_PATTERN, 11);

        // 3. 15位身份证号掩码
        result = replaceWithPattern(result, ID_CARD_15_PATTERN, 8);

        // 4. 手机号掩码
        Matcher phoneMatcher = PHONE_PATTERN.matcher(result);
        StringBuffer sbPhone = new StringBuffer();
        while (phoneMatcher.find()) {
            String masked = phoneMatcher.group(1) + "****" + phoneMatcher.group(2);
            phoneMatcher.appendReplacement(sbPhone, Matcher.quoteReplacement(masked));
        }
        phoneMatcher.appendTail(sbPhone);
        result = sbPhone.toString();

        // 5. 银行卡号掩码
        Matcher bankMatcher = BANK_CARD_PATTERN.matcher(result);
        StringBuffer sbBank = new StringBuffer();
        while (bankMatcher.find()) {
            int maskLen = bankMatcher.group(0).length() - 8;
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < maskLen; i++) {
                stars.append("*");
            }
            String masked = bankMatcher.group(1) + stars.toString() + bankMatcher.group(2);
            bankMatcher.appendReplacement(sbBank, Matcher.quoteReplacement(masked));
        }
        bankMatcher.appendTail(sbBank);
        result = sbBank.toString();

        // 6. 邮箱掩码
        Matcher emailMatcher = EMAIL_PATTERN.matcher(result);
        StringBuffer sbEmail = new StringBuffer();
        while (emailMatcher.find()) {
            String masked = emailMatcher.group(1) + "***" + emailMatcher.group(2);
            emailMatcher.appendReplacement(sbEmail, Matcher.quoteReplacement(masked));
        }
        emailMatcher.appendTail(sbEmail);
        result = sbEmail.toString();

        return result;
    }

    /**
     * 使用指定模式和掩码长度进行替换
     */
    private static String replaceWithPattern(String input, Pattern pattern, int maskLength) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(input, lastEnd, matcher.start());
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < maskLength; i++) {
                stars.append("*");
            }
            result.append(matcher.group(1)).append(stars).append(matcher.group(2));
            lastEnd = matcher.end();
        }
        result.append(input, lastEnd, input.length());
        return result.toString();
    }
}
