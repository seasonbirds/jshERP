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

/**
 * API接口日志采集切面
 * 
 * 功能说明：
 * 1. 采用AOP方式拦截所有外部HTTP接口请求
 * 2. 记录请求详细信息（时间、URL、IP、User-Agent、用户、参数等）
 * 3. 记录响应信息和执行耗时
 * 4. 对敏感信息进行脱敏处理
 * 5. 日志打印到控制台并异步发送到Kafka
 * 6. 处理失败不影响原有接口功能
 * 
 * @author jshERP
 */
@Aspect
@Component
public class ApiLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogAspect.class);

    /**
     * 日期格式化器，用于记录请求时间
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 请求参数最大长度（200KB），超过则截断
     */
    private static final int REQUEST_PARAMS_MAX_SIZE = 200 * 1024;

    /**
     * 响应参数最大长度（500KB），超过则截断
     */
    private static final int RESPONSE_PARAMS_MAX_SIZE = 500 * 1024;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private ApiLogKafkaService apiLogKafkaService;

    /**
     * 是否开启API日志采集，默认开启
     */
    @Value("${api.log.enabled:true}")
    private boolean apiLogEnabled;

    /**
     * 是否开启控制台日志输出，默认开启
     */
    @Value("${api.log.console.enabled:true}")
    private boolean consoleLogEnabled;

    /**
     * 定义切点：拦截所有带有HTTP映射注解的方法
     * 
     * 只拦截以下注解标记的外部接口方法：
     * - @RequestMapping
     * - @GetMapping
     * - @PostMapping
     * - @PutMapping
     * - @DeleteMapping
     * - @PatchMapping
     * 
     * 注意：不拦截Controller中的内部方法（如getMenuByFunction、getFunctionList等）
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.GetMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录日志
     * 
     * 处理流程：
     * 1. 检查是否开启日志采集，未开启则直接执行原方法
     * 2. 记录请求开始时间
     * 3. 构建请求信息（URL、IP、参数等）
     * 4. 执行原方法，捕获异常但不影响原方法执行
     * 5. 记录响应信息和执行耗时
     * 6. 打印日志到控制台
     * 7. 异步发送日志到Kafka
     * 
     * 重要：所有日志处理逻辑的异常都被捕获，确保不影响原有接口功能
     * 
     * @param joinPoint 切点对象
     * @return 原方法返回值
     * @throws Throwable 原方法可能抛出的异常
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果未开启日志采集，直接执行原方法
        if (!apiLogEnabled) {
            return joinPoint.proceed();
        }

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        ApiLog apiLog = new ApiLog();
        Object result = null;
        Throwable exception = null;

        // 构建请求信息
        // 捕获所有异常，确保即使日志处理失败也不影响原方法
        try {
            buildRequestInfo(apiLog, joinPoint);
        } catch (Exception e) {
            logger.error("构建API请求日志信息失败: {}", e.getMessage(), e);
        }

        // 执行原方法
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            // 记录异常，用于后续日志记录
            exception = e;
            // 重新抛出异常，不影响原有的异常处理
            throw e;
        } finally {
            // 在finally块中执行日志处理，确保无论方法正常返回还是抛出异常都能记录日志
            try {
                // 计算接口耗时（毫秒）
                long costTime = System.currentTimeMillis() - startTime;
                apiLog.setCostTime(costTime);
                apiLog.setRequestTime(DATE_FORMAT.format(new Date(startTime)));

                // 构建响应信息
                try {
                    buildResponseInfo(apiLog, result, exception);
                } catch (Exception e) {
                    logger.error("构建API响应日志信息失败: {}", e.getMessage(), e);
                }

                // 处理日志（打印控制台、发送Kafka）
                try {
                    processApiLog(apiLog);
                } catch (Exception e) {
                    logger.error("处理API日志失败: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                // 捕获所有异常，确保日志处理失败不影响主业务
                logger.error("API日志处理流程失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 构建请求信息
     * 
     * 从HttpServletRequest和方法参数中提取以下信息：
     * 1. URL：请求的URI路径
     * 2. IP：客户端IP地址（支持代理服务器）
     * 3. User-Agent：客户端浏览器/设备信息
     * 4. 用户ID：从Redis Session中获取，未登录则为空
     * 5. 请求参数：查询参数 + 请求体参数（文件类型忽略）
     * 
     * 注意：请求参数会进行敏感信息脱敏和长度截断
     * 
     * @param apiLog 日志对象
     * @param joinPoint 切点对象
     */
    private void buildRequestInfo(ApiLog apiLog, ProceedingJoinPoint joinPoint) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return;
        }

        // 设置请求基本信息
        apiLog.setUrl(request.getRequestURI());
        apiLog.setIp(Tools.getIpAddr(request));
        apiLog.setUserAgent(request.getHeader("User-Agent"));

        // 从Redis Session获取当前用户ID
        Object userId = redisService.getObjectFromSessionByKey(request, "userId");
        if (userId != null) {
            apiLog.setUserId(userId.toString());
        }

        // 构建并处理请求参数
        String requestParams = buildRequestParams(joinPoint, request);
        if (StringUtil.isNotEmpty(requestParams)) {
            // 敏感信息脱敏
            String maskedParams = SensitiveInfoMaskUtil.maskSensitiveInfo(requestParams);
            // 长度截断（超过200KB）
            apiLog.setRequestParams(truncateString(maskedParams, REQUEST_PARAMS_MAX_SIZE));
        }
    }

    /**
     * 构建请求参数字符串
     * 
     * 处理逻辑：
     * 1. 从HttpServletRequest获取URL查询参数（?key=value形式）
     * 2. 从方法参数中获取请求体参数（@RequestBody形式）
     * 3. 忽略文件类型参数（MultipartFile、InputStream、OutputStream等）
     * 4. 忽略HttpServletRequest和HttpServletResponse对象
     * 
     * @param joinPoint 切点对象
     * @param request HTTP请求对象
     * @return JSON格式的请求参数字符串
     */
    private String buildRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        Map<String, Object> paramsMap = new HashMap<>();

        // 1. 处理URL查询参数（?key=value形式）
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

        // 2. 处理方法参数中的请求体
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            // 忽略文件类型参数
            if (isFileType(arg)) {
                continue;
            }
            // 忽略HttpServletRequest和HttpServletResponse
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }

            // 将参数转为JSON字符串
            try {
                String argJson = JSON.toJSONString(arg);
                // 只记录非空对象
                if (StringUtil.isNotEmpty(argJson) && !"{}".equals(argJson)) {
                    paramsMap.put("body_" + arg.getClass().getSimpleName(), arg);
                }
            } catch (Exception e) {
                // JSON转换失败时使用toString()
                paramsMap.put("body_" + arg.getClass().getSimpleName(), arg.toString());
            }
        }

        if (paramsMap.isEmpty()) {
            return null;
        }

        return JSON.toJSONString(paramsMap);
    }

    /**
     * 判断对象是否为文件类型
     * 
     * 用于判断是否需要忽略该参数（文件类型参数不记录日志）
     * 
     * @param obj 待判断的对象
     * @return true-是文件类型，false-不是文件类型
     */
    private boolean isFileType(Object obj) {
        if (obj == null) {
            return false;
        }
        // MultipartFile（文件上传）
        if (obj instanceof MultipartFile) {
            return true;
        }
        // 输入流
        if (obj instanceof InputStream) {
            return true;
        }
        // 输出流（文件下载）
        if (obj instanceof OutputStream) {
            return true;
        }
        // 数组类型，检查组件类型
        if (obj.getClass().isArray()) {
            Class<?> componentType = obj.getClass().getComponentType();
            if (MultipartFile.class.isAssignableFrom(componentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建响应信息
     * 
     * 处理逻辑：
     * 1. 如果有异常，响应状态设为0，记录异常信息
     * 2. 如果返回值是文件类型，标记为[FILE_RESPONSE]
     * 3. 否则，将返回值转为JSON字符串
     * 4. 解析响应状态码：
     *    - code=200或code=0，视为成功，状态设为1
     *    - 其他code值，状态设为该code值
     *    - 无code字段，默认视为成功，状态设为1
     * 
     * 注意：响应参数会进行敏感信息脱敏和长度截断
     * 
     * @param apiLog 日志对象
     * @param result 方法返回值
     * @param exception 异常对象（无异常则为null）
     */
    private void buildResponseInfo(ApiLog apiLog, Object result, Throwable exception) {
        int responseStatus = 1;
        String responseParams = null;

        if (exception != null) {
            // 有异常，状态设为0
            responseStatus = 0;
            responseParams = "{\"exception\":\"" + exception.getMessage() + "\"}";
        } else if (result != null) {
            if (isFileType(result)) {
                // 文件类型响应，不记录具体内容
                responseParams = "[FILE_RESPONSE]";
            } else {
                // 将返回值转为JSON字符串
                try {
                    responseParams = JSON.toJSONString(result);
                } catch (Exception e) {
                    // JSON转换失败时使用toString()
                    responseParams = result.toString();
                }

                // 解析响应状态码
                responseStatus = determineResponseStatus(result);
            }
        } else {
            // 无返回值，状态设为0
            responseStatus = 0;
            responseParams = "null";
        }

        apiLog.setResponseStatus(responseStatus);

        // 处理响应参数（脱敏、截断）
        if (StringUtil.isNotEmpty(responseParams) && !"[FILE_RESPONSE]".equals(responseParams)) {
            // 敏感信息脱敏
            String maskedResponse = SensitiveInfoMaskUtil.maskSensitiveInfo(responseParams);
            // 长度截断（超过500KB）
            apiLog.setResponseParams(truncateString(maskedResponse, RESPONSE_PARAMS_MAX_SIZE));
        } else {
            apiLog.setResponseParams(responseParams);
        }
    }

    /**
     * 解析响应状态码
     * 
     * 规则：
     * 1. 尝试将返回值转为JSON对象
     * 2. 检查是否包含code字段
     * 3. 如果code=200或code=0，视为成功，返回1
     * 4. 其他code值，返回该code值
     * 5. 无code字段或解析失败，默认返回1（视为成功）
     * 
     * @param result 方法返回值
     * @return 响应状态码
     */
    private int determineResponseStatus(Object result) {
        if (result == null) {
            return 0;
        }

        try {
            // 将返回值转为JSON字符串再解析
            String resultStr = JSON.toJSONString(result);
            JSONObject jsonObj = JSON.parseObject(resultStr);

            // 检查是否包含code字段
            if (jsonObj.containsKey("code")) {
                Object codeObj = jsonObj.get("code");
                int code = parseCode(codeObj);

                // 根据项目约定，code=200或code=0表示成功
                if (code == 200 || code == 0) {
                    return 1;
                }
                return code;
            }
        } catch (Exception e) {
            // 解析失败，默认视为成功
        }

        // 无code字段，默认视为成功
        return 1;
    }

    /**
     * 解析code对象为int值
     * 
     * 支持以下类型：
     * 1. Number类型（Integer、Long、BigDecimal等）
     * 2. String类型（尝试转换为int）
     * 
     * @param codeObj code对象
     * @return 解析后的int值，解析失败返回-1
     */
    private int parseCode(Object codeObj) {
        if (codeObj == null) {
            return -1;
        }
        // Number类型直接转int
        if (codeObj instanceof Number) {
            return ((Number) codeObj).intValue();
        }
        // 其他类型尝试转换
        try {
            return Integer.parseInt(codeObj.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 处理API日志
     * 
     * 处理流程：
     * 1. 如果开启控制台日志，将日志打印到控制台
     * 2. 如果Kafka服务可用，异步发送日志到Kafka
     * 
     * 注意：所有异常都被捕获，确保日志处理失败不影响主业务
     * 
     * @param apiLog 日志对象
     */
    private void processApiLog(ApiLog apiLog) {
        // 打印到控制台
        if (consoleLogEnabled) {
            logger.info("API请求日志: {}", JSON.toJSONString(apiLog));
        }

        // 异步发送到Kafka
        if (apiLogKafkaService != null) {
            try {
                apiLogKafkaService.sendApiLogToKafka(apiLog);
            } catch (Exception e) {
                // Kafka发送失败只记录日志，不影响主业务
                logger.error("发送API日志到Kafka失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 截断字符串到指定长度
     * 
     * @param str 原始字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串，末尾添加"...[TRUNCATED]"标记
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        // 截断并添加截断标记
        return str.substring(0, maxLength) + "...[TRUNCATED]";
    }

    /**
     * 获取当前HttpServletRequest对象
     * 
     * 从RequestContextHolder中获取当前请求上下文
     * 
     * @return HttpServletRequest对象，如果获取失败则返回null
     */
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
