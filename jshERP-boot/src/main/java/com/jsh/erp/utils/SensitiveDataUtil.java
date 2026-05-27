package com.jsh.erp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<![\\d])(1[3-9]\\d{9})(?![\\d])");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(?<![\\d])([1-9]\\d{5}(?:18|19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx])(?![\\d])");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(?<![\\d])([1-9]\\d{14,18})(?![\\d])");
    private static final String[] PASSWORD_KEYWORDS = {"password", "pwd", "密码", "口令"};

    private SensitiveDataUtil() {
    }

    public static String maskSensitiveData(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        content = maskIdCard(content);
        content = maskPhone(content);
        content = maskEmail(content);
        content = maskBankCard(content);
        content = maskPassword(content);
        return content;
    }

    private static String maskPhone(String content) {
        Matcher matcher = PHONE_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String phone = matcher.group(1);
            String masked = phone.substring(0, 3) + "*******" + phone.substring(10);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskIdCard(String content) {
        Matcher matcher = ID_CARD_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String idCard = matcher.group(1);
            int len = idCard.length();
            int maskLen = len * 2 / 3;
            int start = (len - maskLen) / 2;
            StringBuilder masked = new StringBuilder(idCard.substring(0, start));
            for (int i = 0; i < maskLen; i++) {
                masked.append("*");
            }
            masked.append(idCard.substring(start + maskLen));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskEmail(String content) {
        Matcher matcher = EMAIL_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String email = matcher.group(1);
            int atIndex = email.indexOf("@");
            if (atIndex > 0) {
                String localPart = email.substring(0, atIndex);
                String domain = email.substring(atIndex);
                int maskLen = localPart.length() * 2 / 3;
                if (maskLen < 1) {
                    maskLen = 1;
                }
                int start = 1;
                StringBuilder masked = new StringBuilder(localPart.substring(0, start));
                for (int i = 0; i < maskLen; i++) {
                    masked.append("*");
                }
                if (start + maskLen < localPart.length()) {
                    masked.append(localPart.substring(start + maskLen));
                }
                masked.append(domain);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked.toString()));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskBankCard(String content) {
        Matcher matcher = BANK_CARD_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String card = matcher.group(1);
            int len = card.length();
            if (len >= 15) {
                int maskLen = len * 2 / 3;
                int start = 4;
                StringBuilder masked = new StringBuilder(card.substring(0, start));
                for (int i = 0; i < maskLen; i++) {
                    masked.append("*");
                }
                if (start + maskLen < len) {
                    masked.append(card.substring(start + maskLen));
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked.toString()));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskPassword(String content) {
        String result = content;
        for (String keyword : PASSWORD_KEYWORDS) {
            int idx = result.toLowerCase().indexOf(keyword.toLowerCase());
            while (idx >= 0) {
                int colonIdx = result.indexOf(":", idx);
                if (colonIdx < 0) {
                    colonIdx = result.indexOf("=", idx);
                }
                if (colonIdx < 0) {
                    colonIdx = result.indexOf("\"", idx + keyword.length());
                }
                if (colonIdx >= 0 && colonIdx - idx < keyword.length() + 5) {
                    int valueStart = colonIdx + 1;
                    while (valueStart < result.length() && (result.charAt(valueStart) == ' ' || result.charAt(valueStart) == '"')) {
                        valueStart++;
                    }
                    int valueEnd = valueStart;
                    while (valueEnd < result.length() && result.charAt(valueEnd) != '"' && result.charAt(valueEnd) != ','
                            && result.charAt(valueEnd) != '}' && result.charAt(valueEnd) != ' ' && result.charAt(valueEnd) != '\n') {
                        valueEnd++;
                    }
                    if (valueEnd > valueStart) {
                        String pwdValue = result.substring(valueStart, valueEnd);
                        int maskLen = pwdValue.length() * 2 / 3;
                        if (maskLen < 1) {
                            maskLen = pwdValue.length();
                        }
                        StringBuilder masked = new StringBuilder();
                        for (int i = 0; i < maskLen; i++) {
                            masked.append("*");
                        }
                        if (maskLen < pwdValue.length()) {
                            masked.append(pwdValue.substring(maskLen));
                        }
                        result = result.substring(0, valueStart) + masked.toString() + result.substring(valueEnd);
                    }
                }
                idx = result.toLowerCase().indexOf(keyword.toLowerCase(), idx + keyword.length());
            }
        }
        return result;
    }
}
