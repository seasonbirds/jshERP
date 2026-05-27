package com.jsh.erp.aop;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 标注在Service方法上，通过AOP自动记录审计日志。
 * 日志内容通过contentTemplate配置，支持以下占位符：
 * 1. {参数名} - 按参数名引用方法参数值
 * 2. {0}、{1} - 按参数位置引用方法参数值
 * 3. {参数名.字段名} - 引用方法参数的属性字段（支持JSONObject、Map、POJO的getter）
 * 4. {result.字段名} - 引用方法返回值的属性字段
 * 5. {entityNames} - 批量删除前预加载的实体名称列表（格式：[名称1][名称2]）
 *
 * 示例：
 * <pre>
 *   &#64;BusinessLog(moduleName = "用户", type = LogType.ADD, contentTemplate = "新增{obj.loginName}")
 *   public int insertUser(JSONObject obj, HttpServletRequest request) { ... }
 *
 *   &#64;BusinessLog(moduleName = "商品", type = LogType.DELETE, contentTemplate = "删除{entityNames}")
 *   public int batchDeleteMaterialByIds(String ids) { ... }
 *
 *   &#64;BusinessLog(moduleName = "用户", type = LogType.LOGIN, contentTemplate = "登录{result.loginName}", useResultUserInfo = true)
 *   public Map login(String loginName, String password, HttpServletRequest request) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessLog {

    /**
     * 模块名称，如：用户、商品、仓库、单据 等
     */
    String moduleName();

    /**
     * 操作类型
     */
    LogType type() default LogType.OTHER;

    /**
     * 日志内容模板。
     * 支持 {参数名}、{0}位置、{参数名.字段名}属性访问、{result.字段名}返回值访问、{entityNames}预加载实体名称。
     * 默认为空，此时日志内容仅使用操作类型。
     */
    String contentTemplate() default "";

    /**
     * 是否在日志中记录请求参数（脱敏后）
     */
    boolean logParams() default false;

    /**
     * 是否在日志中记录方法返回值
     */
    boolean logResult() default false;

    /**
     * 是否从方法返回值中获取userId和tenantId。
     * 用于登录等场景：用户尚未登录，session中没有userId，需要从方法返回值中获取。
     * 当设置为true时，切面会从返回值中提取User对象的id和tenantId。
     * 支持返回值为User对象、或包含"user"键的Map。
     */
    boolean useResultUserInfo() default false;
}
