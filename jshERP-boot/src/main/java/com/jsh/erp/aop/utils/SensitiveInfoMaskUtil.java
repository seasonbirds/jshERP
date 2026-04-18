package com.jsh.erp.aop.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.utils.StringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveInfoMaskUtil {

    private static final String MASK_CHAR = "*";

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86)?(1[3-9]\\d{9})");

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{15})|(\\d{17}[\\dXx])");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\d{16,19}");

    private static final String[] SENSITIVE_KEYS = {
            "phone", "mobile", "tel", "telephone", "手机号", "电话",
            "idCard", "idcard", "id_card", "身份证", "身份证号",
            "email", "mail", "邮箱",
            "bankCard", "bankcard", "bank_card", "银行卡", "银行卡号", "account"
    };

    public static String maskSensitiveInfo(String content) {
        if (StringUtil.isEmpty(content)) {
            return content;
        }

        try {
            Object jsonObj = JSON.parse(content);
            if (jsonObj instanceof JSONObject) {
                JSONObject maskedObj = maskJsonObject((JSONObject) jsonObj);
                return maskedObj.toJSONString();
            } else if (jsonObj instanceof JSONArray) {
                JSONArray maskedArray = maskJsonArray((JSONArray) jsonObj);
                return maskedArray.toJSONString();
            }
        } catch (Exception e) {
        }

        return maskTextContent(content);
    }

    private static JSONObject maskJsonObject(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        JSONObject maskedObj = new JSONObject();
        Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                maskedObj.put(key, null);
                continue;
            }

            boolean isSensitiveKey = isSensitiveKey(key);

            if (value instanceof JSONObject) {
                maskedObj.put(key, maskJsonObject((JSONObject) value));
            } else if (value instanceof JSONArray) {
                maskedObj.put(key, maskJsonArray((JSONArray) value));
            } else if (value instanceof String) {
                String strValue = (String) value;
                if (isSensitiveKey) {
                    maskedObj.put(key, maskStringValue(strValue));
                } else {
                    maskedObj.put(key, maskTextContent(strValue));
                }
            } else {
                maskedObj.put(key, value);
            }
        }

        return maskedObj;
    }

    private static JSONArray maskJsonArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        JSONArray maskedArray = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            Object item = jsonArray.get(i);
            if (item instanceof JSONObject) {
                maskedArray.add(maskJsonObject((JSONObject) item));
            } else if (item instanceof JSONArray) {
                maskedArray.add(maskJsonArray((JSONArray) item));
            } else if (item instanceof String) {
                maskedArray.add(maskTextContent((String) item));
            } else {
                maskedArray.add(item);
            }
        }

        return maskedArray;
    }

    private static boolean isSensitiveKey(String key) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        for (String sensitiveKey : SENSITIVE_KEYS) {
            if (lowerKey.contains(sensitiveKey.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static String maskStringValue(String value) {
        if (StringUtil.isEmpty(value)) {
            return value;
        }

        if (isPhoneNumber(value)) {
            return maskPhone(value);
        }
        if (isIdCard(value)) {
            return maskIdCard(value);
        }
        if (isEmail(value)) {
            return maskEmail(value);
        }
        if (isBankCard(value)) {
            return maskBankCard(value);
        }

        return maskText(value);
    }

    private static String maskTextContent(String content) {
        if (StringUtil.isEmpty(content)) {
            return content;
        }

        String result = content;

        result = maskPhoneInText(result);
        result = maskIdCardInText(result);
        result = maskEmailInText(result);
        result = maskBankCardInText(result);

        return result;
    }

    private static boolean isPhoneNumber(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        return PHONE_PATTERN.matcher(value.trim()).matches();
    }

    private static boolean isIdCard(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        String trimmed = value.trim();
        return ID_CARD_PATTERN.matcher(trimmed).matches() && (trimmed.length() == 15 || trimmed.length() == 18);
    }

    private static boolean isEmail(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    private static boolean isBankCard(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        String trimmed = value.trim();
        return BANK_CARD_PATTERN.matcher(trimmed).matches() && trimmed.length() >= 16 && trimmed.length() <= 19;
    }

    public static String maskPhone(String phone) {
        if (StringUtil.isEmpty(phone) || phone.length() < 7) {
            return phone;
        }

        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        int totalLength = cleanPhone.length();
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        int startIndex = (totalLength - maskLength) / 2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cleanPhone.length(); i++) {
            if (i >= startIndex && i < startIndex + maskLength) {
                sb.append(MASK_CHAR);
            } else {
                sb.append(cleanPhone.charAt(i));
            }
        }

        return sb.toString();
    }

    private static String maskPhoneInText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = PHONE_PATTERN.matcher(text);

        while (matcher.find()) {
            String matched = matcher.group(1);
            matcher.appendReplacement(sb, maskPhone(matched));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String maskIdCard(String idCard) {
        if (StringUtil.isEmpty(idCard)) {
            return idCard;
        }

        int totalLength = idCard.length();
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        int startIndex = (totalLength - maskLength) / 2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idCard.length(); i++) {
            if (i >= startIndex && i < startIndex + maskLength) {
                sb.append(MASK_CHAR);
            } else {
                sb.append(idCard.charAt(i));
            }
        }

        return sb.toString();
    }

    private static String maskIdCardInText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile("\\b(\\d{15}|\\d{17}[\\dXx])\\b").matcher(text);

        while (matcher.find()) {
            String matched = matcher.group(1);
            matcher.appendReplacement(sb, maskIdCard(matched));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String maskEmail(String email) {
        if (StringUtil.isEmpty(email)) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        int totalLength = localPart.length();
        if (totalLength <= 1) {
            return MASK_CHAR + domainPart;
        }

        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        int startIndex = (totalLength - maskLength) / 2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < localPart.length(); i++) {
            if (i >= startIndex && i < startIndex + maskLength) {
                sb.append(MASK_CHAR);
            } else {
                sb.append(localPart.charAt(i));
            }
        }

        return sb.toString() + domainPart;
    }

    private static String maskEmailInText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = EMAIL_PATTERN.matcher(text);

        while (matcher.find()) {
            String matched = matcher.group();
            matcher.appendReplacement(sb, maskEmail(matched));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String maskBankCard(String bankCard) {
        if (StringUtil.isEmpty(bankCard)) {
            return bankCard;
        }

        String cleanCard = bankCard.replaceAll("[^0-9]", "");
        int totalLength = cleanCard.length();

        if (totalLength < 10) {
            return bankCard;
        }

        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        int startIndex = (totalLength - maskLength) / 2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cleanCard.length(); i++) {
            if (i >= startIndex && i < startIndex + maskLength) {
                sb.append(MASK_CHAR);
            } else {
                sb.append(cleanCard.charAt(i));
            }
        }

        return sb.toString();
    }

    private static String maskBankCardInText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile("\\b(\\d{16,19})\\b").matcher(text);

        while (matcher.find()) {
            String matched = matcher.group(1);
            matcher.appendReplacement(sb, maskBankCard(matched));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String maskText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        int totalLength = text.length();
        if (totalLength <= 1) {
            return MASK_CHAR;
        }

        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        int startIndex = (totalLength - maskLength) / 2;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (i >= startIndex && i < startIndex + maskLength) {
                sb.append(MASK_CHAR);
            } else {
                sb.append(text.charAt(i));
            }
        }

        return sb.toString();
    }
}
