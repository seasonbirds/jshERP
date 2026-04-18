package com.jsh.erp.aop.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.utils.StringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感信息脱敏工具类
 * 
 * 用于对日志中的敏感信息进行脱敏处理，保护用户隐私
 * 
 * 支持的敏感信息类型：
 * 1. 手机号：11位手机号码（支持+86前缀）
 * 2. 身份证号：15位或18位身份证号
 * 3. 邮箱地址：标准邮箱格式
 * 4. 银行卡号：16-19位银行卡号
 * 
 * 脱敏规则：
 * - 遮蔽敏感信息的2/3以上内容
 * - 保留前后部分信息，中间用*替换
 * - 支持JSON结构和纯文本两种格式
 * 
 * @author jshERP
 */
public class SensitiveInfoMaskUtil {

    /**
     * 脱敏字符（使用*号）
     */
    private static final String MASK_CHAR = "*";

    /**
     * 手机号正则表达式
     * 支持：1[3-9]开头的11位手机号，可选+86前缀
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86)?(1[3-9]\\d{9})");

    /**
     * 身份证号正则表达式
     * 支持：15位或18位身份证号（18位最后一位可为X/x）
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{15})|(\\d{17}[\\dXx])");

    /**
     * 邮箱地址正则表达式
     * 标准邮箱格式：local@domain
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    /**
     * 银行卡号正则表达式
     * 支持：16-19位纯数字银行卡号
     */
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\d{16,19}");

    /**
     * 敏感信息关键字列表
     * 当JSON对象的key包含这些关键字时，对应的值会被脱敏
     */
    private static final String[] SENSITIVE_KEYS = {
            "phone", "mobile", "tel", "telephone", "手机号", "电话",
            "idCard", "idcard", "id_card", "身份证", "身份证号",
            "email", "mail", "邮箱",
            "bankCard", "bankcard", "bank_card", "银行卡", "银行卡号", "account"
    };

    /**
     * 对内容进行敏感信息脱敏
     * 
     * 处理流程：
     * 1. 尝试将内容解析为JSON
     * 2. 如果是JSONObject，递归遍历所有key-value对进行脱敏
     * 3. 如果是JSONArray，遍历所有元素进行脱敏
     * 4. 如果不是JSON或解析失败，按纯文本进行正则匹配脱敏
     * 
     * @param content 待脱敏的内容（JSON或纯文本）
     * @return 脱敏后的内容
     */
    public static String maskSensitiveInfo(String content) {
        if (StringUtil.isEmpty(content)) {
            return content;
        }

        try {
            // 尝试解析为JSON
            Object jsonObj = JSON.parse(content);
            if (jsonObj instanceof JSONObject) {
                // 处理JSON对象
                JSONObject maskedObj = maskJsonObject((JSONObject) jsonObj);
                return maskedObj.toJSONString();
            } else if (jsonObj instanceof JSONArray) {
                // 处理JSON数组
                JSONArray maskedArray = maskJsonArray((JSONArray) jsonObj);
                return maskedArray.toJSONString();
            }
        } catch (Exception e) {
            // JSON解析失败，按纯文本处理
        }

        // 按纯文本进行脱敏
        return maskTextContent(content);
    }

    /**
     * 对JSONObject进行脱敏处理
     * 
     * 递归遍历所有key-value对：
     * 1. 如果key是敏感关键字，对应的值进行脱敏
     * 2. 如果value是JSONObject，递归处理
     * 3. 如果value是JSONArray，遍历处理
     * 4. 如果value是String类型，同时进行文本脱敏
     * 
     * @param jsonObject 待脱敏的JSON对象
     * @return 脱敏后的JSON对象
     */
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

            // 检查key是否为敏感关键字
            boolean isSensitiveKey = isSensitiveKey(key);

            if (value instanceof JSONObject) {
                // 递归处理嵌套的JSON对象
                maskedObj.put(key, maskJsonObject((JSONObject) value));
            } else if (value instanceof JSONArray) {
                // 递归处理JSON数组
                maskedObj.put(key, maskJsonArray((JSONArray) value));
            } else if (value instanceof String) {
                String strValue = (String) value;
                if (isSensitiveKey) {
                    // key是敏感关键字，对值进行脱敏
                    maskedObj.put(key, maskStringValue(strValue));
                } else {
                    // 对值进行文本脱敏（可能包含敏感信息）
                    maskedObj.put(key, maskTextContent(strValue));
                }
            } else {
                // 其他类型保持不变
                maskedObj.put(key, value);
            }
        }

        return maskedObj;
    }

    /**
     * 对JSONArray进行脱敏处理
     * 
     * 遍历数组中的每个元素：
     * 1. 如果是JSONObject，递归处理
     * 2. 如果是JSONArray，递归处理
     * 3. 如果是String类型，进行文本脱敏
     * 
     * @param jsonArray 待脱敏的JSON数组
     * @return 脱敏后的JSON数组
     */
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

    /**
     * 检查key是否为敏感关键字
     * 
     * 忽略大小写进行匹配，只要key包含敏感关键字即认为是敏感字段
     * 
     * @param key JSON对象的key
     * @return true-是敏感关键字，false-不是敏感关键字
     */
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

    /**
     * 对字符串值进行脱敏
     * 
     * 自动识别字符串类型并进行相应的脱敏：
     * 1. 手机号 → maskPhone
     * 2. 身份证号 → maskIdCard
     * 3. 邮箱 → maskEmail
     * 4. 银行卡号 → maskBankCard
     * 5. 其他 → maskText（通用脱敏）
     * 
     * @param value 待脱敏的字符串值
     * @return 脱敏后的字符串
     */
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

        // 通用脱敏
        return maskText(value);
    }

    /**
     * 对纯文本内容进行脱敏
     * 
     * 使用正则表达式匹配文本中的敏感信息：
     * 1. 匹配手机号并脱敏
     * 2. 匹配身份证号并脱敏
     * 3. 匹配邮箱并脱敏
     * 4. 匹配银行卡号并脱敏
     * 
     * @param content 待脱敏的纯文本
     * @return 脱敏后的文本
     */
    private static String maskTextContent(String content) {
        if (StringUtil.isEmpty(content)) {
            return content;
        }

        String result = content;

        // 依次脱敏各种类型的敏感信息
        result = maskPhoneInText(result);
        result = maskIdCardInText(result);
        result = maskEmailInText(result);
        result = maskBankCardInText(result);

        return result;
    }

    /**
     * 检查是否为手机号
     */
    private static boolean isPhoneNumber(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        return PHONE_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * 检查是否为身份证号
     */
    private static boolean isIdCard(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        String trimmed = value.trim();
        return ID_CARD_PATTERN.matcher(trimmed).matches() && (trimmed.length() == 15 || trimmed.length() == 18);
    }

    /**
     * 检查是否为邮箱地址
     */
    private static boolean isEmail(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * 检查是否为银行卡号
     */
    private static boolean isBankCard(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        String trimmed = value.trim();
        return BANK_CARD_PATTERN.matcher(trimmed).matches() && trimmed.length() >= 16 && trimmed.length() <= 19;
    }

    /**
     * 对手机号进行脱敏
     * 
     * 脱敏规则：
     * 1. 保留非数字字符（如+、-等）
     * 2. 对数字部分进行脱敏，遮蔽2/3以上内容
     * 3. 保留开头和结尾部分，中间用*替换
     * 
     * 示例：13812345678 → 138****5678
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (StringUtil.isEmpty(phone) || phone.length() < 7) {
            return phone;
        }

        // 移除非数字和+号的字符
        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        int totalLength = cleanPhone.length();
        // 计算需要遮蔽的长度（2/3以上）
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        // 计算遮蔽起始位置（居中遮蔽）
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

    /**
     * 对文本中的手机号进行脱敏
     */
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

    /**
     * 对身份证号进行脱敏
     * 
     * 脱敏规则：
     * 1. 支持15位和18位身份证号
     * 2. 遮蔽2/3以上内容
     * 3. 居中遮蔽，保留前后部分
     * 
     * 示例：110101199001011234 → 110***********1234
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (StringUtil.isEmpty(idCard)) {
            return idCard;
        }

        int totalLength = idCard.length();
        // 计算需要遮蔽的长度（2/3以上）
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        // 计算遮蔽起始位置（居中遮蔽）
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

    /**
     * 对文本中的身份证号进行脱敏
     */
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

    /**
     * 对邮箱地址进行脱敏
     * 
     * 脱敏规则：
     * 1. 只对@符号前的本地部分进行脱敏
     * 2. 域名部分保持不变
     * 3. 遮蔽本地部分的2/3以上内容
     * 
     * 示例：testuser@example.com → tes***er@example.com
     * 
     * @param email 邮箱地址
     * @return 脱敏后的邮箱地址
     */
    public static String maskEmail(String email) {
        if (StringUtil.isEmpty(email)) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        // 分离本地部分和域名部分
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        int totalLength = localPart.length();
        if (totalLength <= 1) {
            return MASK_CHAR + domainPart;
        }

        // 计算需要遮蔽的长度（2/3以上）
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        // 计算遮蔽起始位置（居中遮蔽）
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

    /**
     * 对文本中的邮箱地址进行脱敏
     */
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

    /**
     * 对银行卡号进行脱敏
     * 
     * 脱敏规则：
     * 1. 支持16-19位银行卡号
     * 2. 移除非数字字符后进行脱敏
     * 3. 遮蔽2/3以上内容
     * 4. 居中遮蔽，保留前后部分
     * 
     * 示例：6222021234567890123 → 6222**********0123
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (StringUtil.isEmpty(bankCard)) {
            return bankCard;
        }

        // 移除非数字字符
        String cleanCard = bankCard.replaceAll("[^0-9]", "");
        int totalLength = cleanCard.length();

        if (totalLength < 10) {
            return bankCard;
        }

        // 计算需要遮蔽的长度（2/3以上）
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        // 计算遮蔽起始位置（居中遮蔽）
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

    /**
     * 对文本中的银行卡号进行脱敏
     */
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

    /**
     * 对普通文本进行通用脱敏
     * 
     * 脱敏规则：
     * 1. 遮蔽2/3以上内容
     * 2. 居中遮蔽，保留前后部分
     * 3. 适用于无法识别具体类型的敏感信息
     * 
     * @param text 待脱敏的文本
     * @return 脱敏后的文本
     */
    private static String maskText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }

        int totalLength = text.length();
        if (totalLength <= 1) {
            return MASK_CHAR;
        }

        // 计算需要遮蔽的长度（2/3以上）
        int maskLength = (int) Math.ceil(totalLength * 2.0 / 3);
        // 计算遮蔽起始位置（居中遮蔽）
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
