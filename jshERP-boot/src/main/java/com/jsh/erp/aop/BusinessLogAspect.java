package com.jsh.erp.aop;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
import com.jsh.erp.service.LogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 审计日志切面
 * 拦截所有标注了@BusinessLog的Service方法，自动记录审计日志。
 * 自动从请求上下文获取当前用户ID、租户ID、客户端IP等信息，不需要业务方法手动传递。
 *
 * 支持的contentTemplate占位符：
 * 1. {参数名} - 按参数名引用方法参数值
 * 2. {0}、{1} - 按参数位置引用方法参数值
 * 3. {参数名.字段名} - 引用方法参数的属性字段（支持JSONObject、Map、POJO的getter）
 * 4. {result.字段名} - 引用方法返回值的属性字段
 * 5. {entityNames} - 批量删除前预加载的实体名称列表
 *
 * 对于批量删除操作，切面会在@Before中预加载实体名称到ThreadLocal中，
 * 在@AfterReturning中使用，确保删除前能获取到实体名称。
 */
@Aspect
@Component
public class BusinessLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(BusinessLogAspect.class);

    /** 预加载的实体名称，用于批量删除操作 */
    private static final ThreadLocal<String> preloadedEntities = new ThreadLocal<>();

    @Resource
    private LogService logService;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 模块名 → Mapper Bean名称 的映射
     * 用于在批量删除操作前预加载实体名称
     */
    private static final Map<String, String> MODULE_MAPPER_MAP = new LinkedHashMap<>();

    /**
     * 模块名 → 实体名称提取函数 的映射
     * 用于从实体对象中提取用于日志记录的名称字段
     */
    private static final Map<String, Function<Object, String>> MODULE_NAME_GETTER_MAP = new LinkedHashMap<>();

    static {
        // 注册各模块的Mapper Bean名称
        MODULE_MAPPER_MAP.put("用户", "userMapper");
        MODULE_MAPPER_MAP.put("财务单据", "accountHeadMapper");
        MODULE_MAPPER_MAP.put("单据", "depotHeadMapper");
        MODULE_MAPPER_MAP.put("仓库", "depotMapper");
        MODULE_MAPPER_MAP.put("商品", "materialMapper");
        MODULE_MAPPER_MAP.put("商家", "supplierMapper");
        MODULE_MAPPER_MAP.put("账户", "accountMapper");
        MODULE_MAPPER_MAP.put("角色", "roleMapper");
        MODULE_MAPPER_MAP.put("经手人", "personMapper");
        MODULE_MAPPER_MAP.put("功能", "functionMapper");
        MODULE_MAPPER_MAP.put("多单位", "unitMapper");
        MODULE_MAPPER_MAP.put("收支项目", "inOutItemMapper");
        MODULE_MAPPER_MAP.put("机构", "organizationMapper");
        MODULE_MAPPER_MAP.put("商品类型", "materialMapper"); // 使用materialMapper查分类名称
        MODULE_MAPPER_MAP.put("消息", "msgMapper");
        MODULE_MAPPER_MAP.put("系统配置", "systemConfigMapper");
        MODULE_MAPPER_MAP.put("序列号", "serialNumberMapper");

        // 注册各模块的实体名称提取函数
        MODULE_NAME_GETTER_MAP.put("用户", e -> safeGet(e, "getLoginName"));
        MODULE_NAME_GETTER_MAP.put("财务单据", e -> safeGet(e, "getBillNo"));
        MODULE_NAME_GETTER_MAP.put("单据", e -> safeGet(e, "getNumber"));
        MODULE_NAME_GETTER_MAP.put("仓库", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("商品", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("商家", e -> safeGet(e, "getSupplier"));
        MODULE_NAME_GETTER_MAP.put("账户", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("角色", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("经手人", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("功能", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("多单位", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("收支项目", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("机构", e -> safeGet(e, "getOrgAbr"));
        MODULE_NAME_GETTER_MAP.put("商品类型", e -> safeGet(e, "getName"));
        MODULE_NAME_GETTER_MAP.put("消息", e -> safeGet(e, "getMsgTitle"));
        MODULE_NAME_GETTER_MAP.put("系统配置", e -> {
            String name = safeGet(e, "getCompanyName");
            return (name != null && !name.isEmpty()) ? name : "配置信息";
        });
        MODULE_NAME_GETTER_MAP.put("序列号", e -> safeGet(e, "getSerialNumber"));
    }

    /**
     * 切点：匹配所有标注了@BusinessLog注解的方法，排除LogService自身以避免循环依赖。
     * 对于DELETE类型的方法，先在@Before中预加载实体名称，再在@AfterReturning中记录日志。
     */
    @Before("@annotation(com.jsh.erp.aop.BusinessLog) && !within(com.jsh.erp.service.LogService)")
    public void beforeMethod(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            BusinessLog businessLog = method.getAnnotation(BusinessLog.class);
            if (businessLog == null) {
                return;
            }
            // 仅对DELETE类型且模板中包含{entityNames}的方法进行预加载
            if (businessLog.type() == LogType.DELETE && businessLog.contentTemplate().contains("{entityNames}")) {
                preloadEntityNames(businessLog.moduleName(), signature, joinPoint.getArgs());
            }
        } catch (Exception e) {
            logger.debug("预加载实体名称失败，将使用降级方案: {}", e.getMessage());
        }
    }

    @AfterReturning(pointcut = "@annotation(com.jsh.erp.aop.BusinessLog) " +
            "&& !within(com.jsh.erp.service.LogService)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, result);
    }

    /**
     * 处理审计日志记录
     */
    private void handleLog(JoinPoint joinPoint, Object result) {
        try {
            // 对于返回int类型的方法，result<=0表示操作失败，不记录日志
            if (result instanceof Integer && (Integer) result <= 0) {
                return;
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            BusinessLog businessLog = method.getAnnotation(BusinessLog.class);
            if (businessLog == null) {
                return;
            }

            // 获取HttpServletRequest
            HttpServletRequest request = getRequest(joinPoint);
            if (request == null) {
                return;
            }

            // 构建日志内容
            String content = buildContent(businessLog, signature, joinPoint.getArgs(), result);

            // 确定userId和tenantId的来源
            if (businessLog.useResultUserInfo()) {
                // 从方法返回值中获取用户信息（用于登录等场景）
                Long userId = null;
                Long tenantId = null;
                if (result instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    Object userObj = resultMap.get("user");
                    if (userObj instanceof User) {
                        User user = (User) userObj;
                        userId = user.getId();
                        tenantId = user.getTenantId();
                    }
                } else if (result instanceof User) {
                    User user = (User) result;
                    userId = user.getId();
                    tenantId = user.getTenantId();
                }
                if (userId != null) {
                    logService.createAuditLog(userId, tenantId, businessLog.moduleName(), content, request);
                }
            } else {
                // 从session中获取用户信息（常规场景）
                logService.createAuditLog(businessLog.moduleName(), content, request);
            }
        } catch (Exception e) {
            // 日志记录失败不影响业务
            logger.error("审计日志记录失败: {}", e.getMessage(), e);
        } finally {
            // 清理ThreadLocal，防止内存泄漏
            preloadedEntities.remove();
        }
    }

    /**
     * 构建日志内容
     * 将contentTemplate中的占位符替换为实际值，并进行敏感数据脱敏
     */
    private String buildContent(BusinessLog businessLog, MethodSignature signature,
                                Object[] args, Object result) {
        StringBuilder content = new StringBuilder(businessLog.type().getValue());

        String template = businessLog.contentTemplate();
        if (template != null && !template.isEmpty()) {
            String resolved = resolveTemplate(template, signature, args, result);
            content.append(resolved);
        }

        // 对日志内容进行敏感数据脱敏
        return SensitiveDataMasker.mask(content.toString());
    }

    /**
     * 解析内容模板，替换占位符
     * 支持多种占位符格式：
     * 1. {参数名} - 按参数名匹配
     * 2. {0}、{1} - 按参数位置匹配
     * 3. {参数名.字段名} - 按参数名+属性路径匹配（支持JSONObject/Map/POJO）
     * 4. {result} - 引用方法返回值
     * 5. {result.字段名} - 引用方法返回值的属性字段
     * 6. {entityNames} - 预加载的实体名称列表
     */
    private String resolveTemplate(String template, MethodSignature signature,
                                   Object[] args, Object result) {
        String resolved = template;
        String[] paramNames = signature.getParameterNames();

        // 1. 处理 {entityNames} 占位符 - 从ThreadLocal获取预加载的实体名称
        if (resolved.contains("{entityNames}")) {
            String names = preloadedEntities.get();
            resolved = resolved.replace("{entityNames}", names != null ? names : "");
        }

        // 2. 处理 {result.xxx} 占位符 - 引用返回值属性
        resolved = resolveResultPlaceholders(resolved, result);

        // 3. 按参数名替换 {paramName.fieldName} 和 {paramName}
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                // 先处理带点号的 {paramName.fieldName}
                String dotPrefix = "{" + paramNames[i] + ".";
                if (resolved.contains(dotPrefix)) {
                    resolved = resolveDotNotiation(resolved, dotPrefix, args[i]);
                }
                // 再处理简单的 {paramName}
                String placeholder = "{" + paramNames[i] + "}";
                if (resolved.contains(placeholder)) {
                    String value = safeToString(args[i]);
                    resolved = resolved.replace(placeholder, value);
                }
            }
        }

        // 4. 按位置替换 {0}、{1}... (包括 {0.field} 格式)
        for (int i = 0; i < args.length; i++) {
            // 处理 {N.field} 格式
            String dotPrefix = "{" + i + ".";
            if (resolved.contains(dotPrefix)) {
                resolved = resolveDotNotiation(resolved, dotPrefix, args[i]);
            }
            // 处理 {N} 格式
            String placeholder = "{" + i + "}";
            if (resolved.contains(placeholder)) {
                String value = safeToString(args[i]);
                resolved = resolved.replace(placeholder, value);
            }
        }

        // 5. 处理 {result} 占位符 - 引用方法返回值
        if (resolved.contains("{result}") && result != null) {
            resolved = resolved.replace("{result}", safeToString(result));
        }

        return resolved;
    }

    /**
     * 解析 {result.xxx} 格式的占位符
     */
    private String resolveResultPlaceholders(String template, Object result) {
        if (result == null || !template.contains("{result.")) {
            return template;
        }
        String resolved = template;
        int searchFrom = 0;
        while (true) {
            int start = resolved.indexOf("{result.", searchFrom);
            if (start < 0) break;
            int end = resolved.indexOf('}', start + 8);
            if (end < 0) break;
            String placeholder = resolved.substring(start, end + 1);
            String fieldPath = resolved.substring(start + 8, end); // 去掉 "{result."
            String value = resolvePropertyPath(result, fieldPath);
            String replacement = value != null ? value : "";
            resolved = resolved.substring(0, start) + replacement + resolved.substring(end + 1);
            searchFrom = start + replacement.length();
        }
        return resolved;
    }

    /**
     * 解析点号表示法的占位符，如 {obj.billNo}
     * 将模板中所有以 dotPrefix 开头、以 } 结尾的占位符替换为实际属性值
     */
    private String resolveDotNotiation(String template, String dotPrefix, Object arg) {
        String resolved = template;
        int searchFrom = 0;
        while (true) {
            int start = resolved.indexOf(dotPrefix, searchFrom);
            if (start < 0) break;
            int end = resolved.indexOf('}', start + dotPrefix.length());
            if (end < 0) break;
            String placeholder = resolved.substring(start, end + 1);
            String fieldName = resolved.substring(start + dotPrefix.length(), end);
            String value = resolveProperty(arg, fieldName);
            String replacement = value != null ? value : "";
            resolved = resolved.substring(0, start) + replacement + resolved.substring(end + 1);
            searchFrom = start + replacement.length();
        }
        return resolved;
    }

    /**
     * 解析属性路径（支持嵌套路径如 "user.id"）
     */
    private String resolvePropertyPath(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return "";
        }
        String[] parts = path.split("\\.");
        Object current = obj;
        for (String part : parts) {
            if (current == null) return "";
            current = getProperty(current, part);
        }
        return current != null ? safeToString(current) : "";
    }

    /**
     * 从对象中获取指定属性的值
     * 支持 JSONObject、Map、POJO (通过getter方法)、JSON字符串（自动解析）
     */
    private Object getProperty(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }
        try {
            // String类型 - 尝试解析为JSON对象后获取字段
            if (obj instanceof String) {
                String str = ((String) obj).trim();
                if (str.startsWith("{")) {
                    JSONObject jsonObj = JSONObject.parseObject(str);
                    return jsonObj.get(fieldName);
                }
                return null;
            }
            // JSONObject (也是Map的子类，优先处理)
            if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                Object value = jsonObj.get(fieldName);
                return value;
            }
            // Map
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                return map.get(fieldName);
            }
            // POJO - 通过getter方法
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method getter = obj.getClass().getMethod(getterName);
            return getter.invoke(obj);
        } catch (Exception e) {
            logger.debug("获取属性 {}.{} 失败: {}", obj.getClass().getSimpleName(), fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * 从对象中获取指定属性的字符串值
     */
    private String resolveProperty(Object obj, String fieldName) {
        Object value = getProperty(obj, fieldName);
        return value != null ? safeToString(value) : "";
    }

    /**
     * 预加载实体名称（用于批量删除操作）。
     * 在@Before阶段，根据模块名找到对应的Mapper，按ID查询实体并提取名称。
     * 结果存储在ThreadLocal中，供@AfterReturning使用。
     */
    private void preloadEntityNames(String moduleName, MethodSignature signature, Object[] args) {
        String mapperBeanName = MODULE_MAPPER_MAP.get(moduleName);
        Function<Object, String> nameGetter = MODULE_NAME_GETTER_MAP.get(moduleName);
        if (mapperBeanName == null || nameGetter == null) {
            return;
        }

        // 从方法参数中解析ID列表
        List<Long> ids = parseIdsFromArgs(signature, args);
        if (ids.isEmpty()) {
            return;
        }

        // 获取Mapper Bean
        Object mapper;
        try {
            mapper = applicationContext.getBean(mapperBeanName);
        } catch (Exception e) {
            logger.debug("无法获取Mapper Bean: {}", mapperBeanName);
            return;
        }

        // 查找selectByPrimaryKey方法。
        // MyBatis Mapper Bean是JDK动态代理，getClass()返回代理类，
        // 代理类不直接声明接口方法，需要通过接口查找。
        Method selectMethod = findMethod(mapper, "selectByPrimaryKey", Long.class);
        if (selectMethod == null) {
            logger.debug("Mapper {} 没有 selectByPrimaryKey(Long) 方法", mapperBeanName);
            return;
        }

        // 查询实体并提取名称
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            try {
                Object entity = selectMethod.invoke(mapper, id);
                if (entity != null) {
                    String name = nameGetter.apply(entity);
                    if (name != null && !name.isEmpty()) {
                        sb.append("[").append(name).append("]");
                    }
                }
            } catch (Exception e) {
                logger.debug("预加载实体 ID={} 名称失败: {}", id, e.getMessage());
            }
        }

        if (sb.length() > 0) {
            preloadedEntities.set(sb.toString());
        }
    }

    /**
     * 在对象（可能是JDK动态代理）上查找指定方法。
     * 先尝试从类本身查找，失败后遍历所有接口查找。
     */
    private Method findMethod(Object target, String methodName, Class<?>... paramTypes) {
        // 1. 尝试从类本身获取
        try {
            return target.getClass().getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // 代理类不直接声明方法，继续从接口查找
        }
        // 2. 从接口查找（MyBatis Mapper Bean是JDK动态代理，方法声明在接口上）
        for (Class<?> iface : target.getClass().getInterfaces()) {
            try {
                return iface.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                // 继续尝试下一个接口
            }
        }
        return null;
    }

    /**
     * 从方法参数中解析ID列表。
     * 查找参数名包含"id"或"Ids"的String参数（逗号分隔的ID串），或查找第一个String类型的ids参数。
     */
    private List<Long> parseIdsFromArgs(MethodSignature signature, Object[] args) {
        List<Long> ids = new ArrayList<>();
        String[] paramNames = signature.getParameterNames();

        // 优先按参数名查找ids参数
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                String name = paramNames[i].toLowerCase();
                if ((name.contains("id") || name.contains("ids")) && args[i] instanceof String) {
                    ids = parseCommaSeparatedIds((String) args[i]);
                    if (!ids.isEmpty()) return ids;
                }
            }
        }

        // 降级：查找第一个String类型的参数
        for (Object arg : args) {
            if (arg instanceof String) {
                ids = parseCommaSeparatedIds((String) arg);
                if (!ids.isEmpty()) return ids;
            }
        }
        return ids;
    }

    /**
     * 解析逗号分隔的ID字符串
     */
    private List<Long> parseCommaSeparatedIds(String idsStr) {
        List<Long> ids = new ArrayList<>();
        if (idsStr == null || idsStr.trim().isEmpty()) {
            return ids;
        }
        try {
            for (String idStr : idsStr.split(",")) {
                idStr = idStr.trim();
                if (!idStr.isEmpty()) {
                    ids.add(Long.parseLong(idStr));
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("解析ID字符串失败: {}", idsStr);
        }
        return ids;
    }

    /**
     * 安全地调用实体的getter方法获取字符串值
     */
    private static String safeGet(Object entity, String getterName) {
        try {
            Method getter = findMethodStatic(entity, getterName);
            if (getter == null) return "";
            Object value = getter.invoke(entity);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 在对象上查找无参方法（可能是JDK动态代理）。
     */
    private static Method findMethodStatic(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            for (Class<?> iface : target.getClass().getInterfaces()) {
                try {
                    return iface.getMethod(methodName);
                } catch (NoSuchMethodException ex) {
                    // continue
                }
            }
        }
        return null;
    }

    /**
     * 从方法参数中获取HttpServletRequest
     * 如果方法参数中没有HttpServletRequest，则从RequestContextHolder中获取
     */
    private HttpServletRequest getRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest) {
                    return (HttpServletRequest) arg;
                }
            }
        }
        // 从RequestContextHolder中获取
        try {
            org.springframework.web.context.request.RequestAttributes ra =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (ra != null) {
                return ((org.springframework.web.context.request.ServletRequestAttributes) ra).getRequest();
            }
        } catch (Exception e) {
            logger.debug("无法从RequestContextHolder获取HttpServletRequest");
        }
        return null;
    }

    /**
     * 安全的对象转字符串方法
     * 过滤掉HttpServletRequest、HttpServletResponse等不适合记录的对象
     */
    private String safeToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof HttpServletRequest || obj instanceof javax.servlet.http.HttpServletResponse) {
            return "";
        }
        if (obj instanceof org.springframework.web.multipart.MultipartFile) {
            return ((org.springframework.web.multipart.MultipartFile) obj).getOriginalFilename();
        }
        return obj.toString();
    }
}
