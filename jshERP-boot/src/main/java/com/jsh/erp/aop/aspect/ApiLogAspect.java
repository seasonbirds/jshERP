package com.jsh.erp.aop.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.aop.entity.ApiLog;
import com.jsh.erp.aop.service.ApiLogKafkaService;
import com.jsh.erp.aop.utils.SensitiveInfoMaskUtil;
import com.jsh.erp.service.RedisService;
import com.jsh.erp.utils.StringUtil;
import com.jsh.erp.utils.Tools;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class ApiLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogAspect.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final int REQUEST_PARAMS_MAX_SIZE = 200 * 1024;
    private static final int RESPONSE_PARAMS_MAX_SIZE = 500 * 1024;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private ApiLogKafkaService apiLogKafkaService;

    @Value("${api.log.enabled:true}")
    private boolean apiLogEnabled;

    @Value("${api.log.console.enabled:true}")
    private boolean consoleLogEnabled;

    @Pointcut("execution(public * com.jsh.erp.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!apiLogEnabled) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        ApiLog apiLog = new ApiLog();
        Object result = null;
        Throwable exception = null;

        try {
            buildRequestInfo(apiLog, joinPoint);
        } catch (Exception e) {
            logger.error("构建API请求日志信息失败: {}", e.getMessage(), e);
        }

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            try {
                long costTime = System.currentTimeMillis() - startTime;
                apiLog.setCostTime(costTime);
                apiLog.setRequestTime(DATE_FORMAT.format(new Date(startTime)));

                try {
                    buildResponseInfo(apiLog, result, exception);
                } catch (Exception e) {
                    logger.error("构建API响应日志信息失败: {}", e.getMessage(), e);
                }

                try {
                    processApiLog(apiLog);
                } catch (Exception e) {
                    logger.error("处理API日志失败: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error("API日志处理流程失败: {}", e.getMessage(), e);
            }
        }
    }

    private void buildRequestInfo(ApiLog apiLog, ProceedingJoinPoint joinPoint) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return;
        }

        apiLog.setUrl(request.getRequestURI());
        apiLog.setIp(Tools.getIpAddr(request));
        apiLog.setUserAgent(request.getHeader("User-Agent"));

        Object userId = redisService.getObjectFromSessionByKey(request, "userId");
        if (userId != null) {
            apiLog.setUserId(userId.toString());
        }

        String requestParams = buildRequestParams(joinPoint, request);
        if (StringUtil.isNotEmpty(requestParams)) {
            String maskedParams = SensitiveInfoMaskUtil.maskSensitiveInfo(requestParams);
            apiLog.setRequestParams(truncateString(maskedParams, REQUEST_PARAMS_MAX_SIZE));
        }
    }

    private String buildRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        Map<String, Object> paramsMap = new HashMap<>();

        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] values = entry.getValue();
                if (values != null && values.length > 0) {
                    if (values.length == 1) {
                        paramsMap.put(entry.getKey(), values[0]);
                    } else {
                        paramsMap.put(entry.getKey(), values);
                    }
                }
            }
        }

        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (isFileType(arg)) {
                continue;
            }
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }

            try {
                String argJson = JSON.toJSONString(arg);
                if (StringUtil.isNotEmpty(argJson) && !"{}".equals(argJson)) {
                    paramsMap.put("body_" + arg.getClass().getSimpleName(), arg);
                }
            } catch (Exception e) {
                paramsMap.put("body_" + arg.getClass().getSimpleName(), arg.toString());
            }
        }

        if (paramsMap.isEmpty()) {
            return null;
        }

        return JSON.toJSONString(paramsMap);
    }

    private boolean isFileType(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MultipartFile) {
            return true;
        }
        if (obj instanceof InputStream) {
            return true;
        }
        if (obj instanceof OutputStream) {
            return true;
        }
        if (obj.getClass().isArray()) {
            Class<?> componentType = obj.getClass().getComponentType();
            if (MultipartFile.class.isAssignableFrom(componentType)) {
                return true;
            }
        }
        return false;
    }

    private void buildResponseInfo(ApiLog apiLog, Object result, Throwable exception) {
        int responseStatus = 1;
        String responseParams = null;

        if (exception != null) {
            responseStatus = 0;
            responseParams = "{\"exception\":\"" + exception.getMessage() + "\"}";
        } else if (result != null) {
            if (isFileType(result)) {
                responseParams = "[FILE_RESPONSE]";
            } else {
                try {
                    responseParams = JSON.toJSONString(result);
                } catch (Exception e) {
                    responseParams = result.toString();
                }

                responseStatus = determineResponseStatus(result);
            }
        } else {
            responseStatus = 0;
            responseParams = "null";
        }

        apiLog.setResponseStatus(responseStatus);

        if (StringUtil.isNotEmpty(responseParams) && !"[FILE_RESPONSE]".equals(responseParams)) {
            String maskedResponse = SensitiveInfoMaskUtil.maskSensitiveInfo(responseParams);
            apiLog.setResponseParams(truncateString(maskedResponse, RESPONSE_PARAMS_MAX_SIZE));
        } else {
            apiLog.setResponseParams(responseParams);
        }
    }

    private int determineResponseStatus(Object result) {
        if (result == null) {
            return 0;
        }

        try {
            String resultStr = JSON.toJSONString(result);
            JSONObject jsonObj = JSON.parseObject(resultStr);

            if (jsonObj.containsKey("code")) {
                Object codeObj = jsonObj.get("code");
                int code = parseCode(codeObj);

                if (code == 200 || code == 0) {
                    return 1;
                }
                return code;
            }
        } catch (Exception e) {
        }

        return 1;
    }

    private int parseCode(Object codeObj) {
        if (codeObj == null) {
            return -1;
        }
        if (codeObj instanceof Number) {
            return ((Number) codeObj).intValue();
        }
        try {
            return Integer.parseInt(codeObj.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void processApiLog(ApiLog apiLog) {
        if (consoleLogEnabled) {
            logger.info("API请求日志: {}", JSON.toJSONString(apiLog));
        }

        if (apiLogKafkaService != null) {
            try {
                apiLogKafkaService.sendApiLogToKafka(apiLog);
            } catch (Exception e) {
                logger.error("发送API日志到Kafka失败: {}", e.getMessage(), e);
            }
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...[TRUNCATED]";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            logger.warn("获取当前HttpServletRequest失败: {}", e.getMessage());
        }
        return null;
    }
}
