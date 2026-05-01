# 国产开源ERP-管伊佳代码优化建议

## 一、安全修复类

### 1.1 密码加密方式优化（高优先级）

**问题描述：** 当前使用MD5加密密码，无盐值，存在彩虹表攻击风险。

**优化前代码：**

位置：`jshERP-boot/src/main/java/com/jsh/erp/utils/Tools.java`

```java
public static String md5Encryp(String str) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(str.getBytes());
    return new BigInteger(1, md.digest()).toString(16);
}
```

位置：`jshERP-boot/src/main/java/com/jsh/erp/service/UserService.java:159`

```java
String password = "123456";
password = Tools.md5Encryp(password);
user.setPassword(password);
```

**优化后代码：**

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoder {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 使用BCrypt加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

**使用示例：**

```java
// 注册/修改密码时
String rawPassword = "123456";
String encodedPassword = PasswordEncoder.encode(rawPassword);
user.setPassword(encodedPassword);

// 登录验证时
if (PasswordEncoder.matches(inputPassword, user.getPassword())) {
    // 验证成功
}
```

**依赖添加（pom.xml）：**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

### 1.2 CSRF防护实现（高优先级）

**问题描述：** 系统完全没有CSRF防护机制。

**优化方案：**

**1. 添加CSRF Token生成和验证过滤器：**

```java
package com.jsh.erp.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class CsrfTokenFilter extends OncePerRequestFilter {
    
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    private static final String CSRF_TOKEN_COOKIE = "CSRF-Token";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     FilterChain filterChain)
            throws ServletException, IOException {
        
        // 只对状态改变的请求进行CSRF验证
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || 
            "OPTIONS".equals(method) || "TRACE".equals(method)) {
            // 生成CSRF Token（如果不存在）
            String csrfToken = (String) request.getAttribute(CSRF_TOKEN_COOKIE);
            if (csrfToken == null) {
                csrfToken = UUID.randomUUID().toString().replace("-", "");
                request.setAttribute(CSRF_TOKEN_COOKIE, csrfToken);
                // 将Token放入响应头供前端获取
                response.setHeader(CSRF_TOKEN_HEADER, csrfToken);
            }
            filterChain.doFilter(request, response);
            return;
        }
        
        // POST/PUT/DELETE等请求需要验证CSRF Token
        String requestCsrfToken = request.getHeader(CSRF_TOKEN_HEADER);
        String sessionCsrfToken = (String) request.getAttribute(CSRF_TOKEN_COOKIE);
        
        if (sessionCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("CSRF Token验证失败");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**2. 前端请求添加CSRF Token：**

位置：`jshERP-web/src/utils/request.js`

```javascript
// request interceptor
service.interceptors.request.use(config => {
    const token = Vue.ls.get(ACCESS_TOKEN)
    if (token) {
        config.headers['X-Access-Token'] = token
    }
    // 添加CSRF Token
    const csrfToken = Vue.ls.get('CSRF-Token')
    if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken
    }
    return config
}, (error) => {
    return Promise.reject(error)
})
```

---

### 1.3 XSS防护实现（中高优先级）

**问题描述：** 系统缺乏输入过滤和输出编码机制。

**优化方案：**

**1. 添加XSS过滤工具类：**

```java
package com.jsh.erp.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class XssFilterUtil {
    
    /**
     * 过滤HTML标签（用于存储前）
     */
    public static String cleanHtml(String html) {
        if (html == null) {
            return null;
        }
        // 使用Jsoup清理HTML，只允许纯文本
        return Jsoup.clean(html, Whitelist.none());
    }
    
    /**
     * 富文本过滤（允许部分HTML标签）
     */
    public static String cleanRichText(String html) {
        if (html == null) {
            return null;
        }
        Whitelist whitelist = Whitelist.basicWithImages()
            .addTags("div", "span", "p", "br")
            .addAttributes(":all", "style", "class");
        return Jsoup.clean(html, whitelist);
    }
    
    /**
     * HTML编码（用于输出到页面）
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(text);
    }
    
    /**
     * JavaScript编码（用于输出到<script>中）
     */
    public static String escapeJavaScript(String text) {
        if (text == null) {
            return null;
        }
        return StringEscapeUtils.escapeEcmaScript(text);
    }
}
```

**2. 在Controller层使用：**

```java
@PostMapping(value = "/add")
public String addResource(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
    // 过滤用户输入
    String name = XssFilterUtil.cleanHtml(obj.getString("name"));
    String remark = XssFilterUtil.cleanHtml(obj.getString("remark"));
    
    // 继续处理...
}
```

**3. 前端Vue模板自动转义：**

Vue的{{ }}语法已经会自动HTML转义，但使用v-html时需要特别注意：

```vue
<template>
    <!-- 安全：{{ }} 会自动转义 -->
    <div>{{ user.name }}</div>
    
    <!-- 风险：v-html 不会转义，必须确保内容安全 -->
    <div v-html="safeContent"></div>
    
    <!-- 错误示例：不要在v-html中使用用户输入 -->
    <div v-html="userInput"></div>
</template>
```

---

### 1.4 点击劫持防护（中优先级）

**问题描述：** 没有设置X-Frame-Options等安全响应头。

**优化方案：**

**1. 添加安全响应头过滤器：**

```java
package com.jsh.erp.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SecurityHeaderFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {
        
        // 防止点击劫持
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        
        // 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // XSS防护（IE浏览器）
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // CSP内容安全策略（根据实际情况调整）
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:;");
        
        // HSTS强制HTTPS（生产环境使用）
        // response.setHeader("Strict-Transport-Security", 
        //     "max-age=31536000; includeSubDomains");
        
        filterChain.doFilter(request, response);
    }
}
```

**2. 注册过滤器：**

```java
package com.jsh.erp.config;

import com.jsh.erp.filter.SecurityHeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilter() {
        FilterRegistrationBean<SecurityHeaderFilter> registrationBean = 
            new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeaderFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
```

---

### 1.5 第三方库版本升级（高优先级）

**问题描述：** 使用了多个存在已知安全漏洞的第三方库。

**优化方案：**

**pom.xml 依赖版本升级：**

```xml
<properties>
    <java.version>1.8</java.version>
    <fastjson.version>2.0.43</fastjson.version>
    <spring-boot.version>2.7.18</spring-boot.version>
    <log4j.version>2.22.1</log4j.version>
    <httpclient.version>4.5.14</httpclient.version>
    <mybatis-plus.version>3.5.3.2</mybatis-plus.version>
    <itextpdf.version>5.5.13.3</itextpdf.version>
</properties>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>${spring-boot.version}</version>
</parent>

<dependencies>
    <!-- Fastjson 2.x 更安全 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>${fastjson.version}</version>
    </dependency>
    
    <!-- Log4j 升级 -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-to-slf4j</artifactId>
        <version>${log4j.version}</version>
    </dependency>
    
    <!-- HttpClient 升级 -->
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
        <version>${httpclient.version}</version>
    </dependency>
    
    <!-- MyBatis-Plus 升级 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>${mybatis-plus.version}</version>
    </dependency>
    
    <!-- iTextPDF 升级 -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itextpdf</artifactId>
        <version>${itextpdf.version}</version>
    </dependency>
</dependencies>
```

**package.json 前端依赖升级：**

```json
{
  "dependencies": {
    "axios": "^1.6.0",
    "vue": "^2.7.16",
    "ant-design-vue": "^1.7.8"
  }
}
```

---

## 二、API接口权限验证（高优先级）

### 2.1 问题分析

**当前问题：**

位置：`jshERP-boot/src/main/java/com/jsh/erp/filter/LogCostFilter.java:36`

```java
@Override
public void doFilter(ServletRequest request, ServletResponse response,
                     FilterChain chain) throws IOException, ServletException {
    // 只验证是否登录
    Object userId = redisService.getObjectFromSessionByKey(servletRequest, "userId");
    if (userId != null) {
        chain.doFilter(request, response);  // 已登录就放行
        return;
    }
    // ...
}
```

**风险：**
- 只验证是否登录，没有验证接口权限
- 用户可以通过直接访问URL绕过前端按钮权限控制
- 存在水平越权（A用户查看B用户数据）和垂直越权（普通用户访问管理员接口）风险

### 2.2 优化方案

**方案：使用AOP切面实现接口级权限验证**

**1. 定义权限注解：**

```java
package com.jsh.erp.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    /**
     * 权限标识（菜单URL）
     */
    String value();
    
    /**
     * 需要的按钮权限（如：add, edit, delete, approve）
     */
    String[] buttons() default {};
}
```

**2. 实现权限验证切面：**

```java
package com.jsh.erp.aspect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.annotation.RequiresPermission;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.exception.BusinessRunTimeException;
import com.jsh.erp.service.RedisService;
import com.jsh.erp.service.UserBusinessService;
import com.jsh.erp.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
public class PermissionAspect {
    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);
    
    @Resource
    private UserService userService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private UserBusinessService userBusinessService;
    
    @Around("@annotation(com.jsh.erp.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) 
            RequestContextHolder.getRequestAttributes()).getRequest();
        
        // 获取当前用户
        Long userId = (Long) redisService.getObjectFromSessionByKey(request, "userId");
        if (userId == null) {
            throw new BusinessRunTimeException(401, "用户未登录");
        }
        
        User currentUser = userService.getUser(userId);
        
        // 超级管理员跳过权限验证
        if ("admin".equals(currentUser.getLoginName())) {
            return point.proceed();
        }
        
        // 获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        
        // 验证菜单权限
        String menuUrl = annotation.value();
        if (!hasMenuPermission(userId, menuUrl)) {
            logger.warn("用户[{}]无权限访问: {}", currentUser.getLoginName(), menuUrl);
            throw new BusinessRunTimeException(403, "无权限访问该接口");
        }
        
        // 验证按钮权限
        String[] requiredButtons = annotation.buttons();
        if (requiredButtons.length > 0 && 
            !hasButtonPermission(userId, menuUrl, requiredButtons)) {
            logger.warn("用户[{}]无按钮权限: {} - {}", 
                currentUser.getLoginName(), menuUrl, requiredButtons);
            throw new BusinessRunTimeException(403, "无权限执行该操作");
        }
        
        return point.proceed();
    }
    
    /**
     * 验证菜单权限
     */
    private boolean hasMenuPermission(Long userId, String menuUrl) {
        // TODO: 实现菜单权限验证逻辑
        // 查询用户角色 -> 角色拥有的功能列表
        // 检查menuUrl是否在功能列表中
        return true;
    }
    
    /**
     * 验证按钮权限
     */
    private boolean hasButtonPermission(Long userId, String menuUrl, String[] buttons) {
        // TODO: 实现按钮权限验证逻辑
        // 查询用户的按钮权限字符串
        // 检查buttons是否在权限列表中
        return true;
    }
}
```

**3. 在Controller中使用注解：**

```java
@RestController
@RequestMapping(value = "/depotHead")
@Api(tags = {"单据管理"})
public class DepotHeadController extends BaseController {
    
    @GetMapping(value = "/list")
    @ApiOperation(value = "获取信息列表")
    @RequiresPermission(value = "/bill/purchase_in", buttons = {})
    public TableDataInfo getList(...) throws Exception {
        // ...
    }
    
    @PostMapping(value = "/addDepotHeadAndDetail")
    @ApiOperation(value = "新增单据主表及单据子表信息")
    @RequiresPermission(value = "/bill/purchase_in", buttons = {"add"})
    public Object addDepotHeadAndDetail(...) throws Exception {
        // ...
    }
    
    @PutMapping(value = "/updateDepotHeadAndDetail")
    @ApiOperation(value = "更新单据主表及单据子表信息")
    @RequiresPermission(value = "/bill/purchase_in", buttons = {"edit"})
    public Object updateDepotHeadAndDetail(...) throws Exception {
        // ...
    }
    
    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "删除")
    @RequiresPermission(value = "/bill/purchase_in", buttons = {"delete"})
    public String deleteResource(...) throws Exception {
        // ...
    }
    
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "批量设置状态-审核或者反审核")
    @RequiresPermission(value = "/bill/purchase_in", buttons = {"approve"})
    public String batchSetStatus(...) throws Exception {
        // ...
    }
}
```

---

## 三、代码风格与规范

### 3.1 SimpleDateFormat线程安全问题

**问题描述：** SimpleDateFormat不是线程安全的，但代码中多处直接创建新实例或作为静态变量使用。

**优化前代码：**

位置：`jshERP-boot/src/main/java/com/jsh/erp/utils/Tools.java:39`

```java
public static String getNow() {
    return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
}

public static String getNow3() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
}
```

**优化后代码：**

```java
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tools {
    // 使用Java 8+的DateTimeFormatter（线程安全）
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM");
    
    /**
     * 获得当天日期，格式为yyyy-MM-dd
     */
    public static String getNow() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    /**
     * 获得当前时间，格式为yyyy-MM-dd HH:mm:ss
     */
    public static String getNow3() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }
    
    /**
     * 获取当前月 yyyy-MM
     */
    public static String getCurrentMonth() {
        return LocalDate.now().format(YEAR_MONTH_FORMATTER);
    }
    
    /**
     * 日期转字符串
     */
    public static String dateToStr(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "";
    }
    
    /**
     * 日期时间转字符串
     */
    public static String dateTimeToStr(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? dateTime.format(formatter) : "";
    }
}
```

**需要兼容Date类型时：**

```java
import java.time.ZoneId;
import java.util.Date;

public static Date localDateTimeToDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
}

public static LocalDateTime dateToLocalDateTime(Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
}
```

---

### 3.2 代码规范统一

**问题描述：** 代码中存在多种命名风格和编码习惯。

**优化建议：**

**1. 添加统一的代码规范配置文件：**

**checkstyle.xml：**

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <!-- 编码格式 -->
    <module name="NewlineAtEndOfFile"/>
    
    <module name="TreeWalker">
        <!-- 命名规范 -->
        <module name="ConstantName"/>
        <module name="LocalFinalVariableName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="TypeName"/>
        
        <!-- 导入规范 -->
        <module name="AvoidStarImport"/>
        <module name="UnusedImports"/>
        
        <!-- 代码块 -->
        <module name="EmptyBlock"/>
        <module name="LeftCurly"/>
        <module name="RightCurly"/>
        
        <!-- 空行 -->
        <module name="EmptyLineSeparator"/>
        <module name="GenericWhitespace"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>
        
        <!-- 注解使用 -->
        <module name="AnnotationUseStyle"/>
    </module>
</module>
```

**2. 统一日志输出：**

**优化前：**

```java
private Logger logger = LoggerFactory.getLogger(UserController.class);

logger.error(">>>>>>>>>>>>>修改用户ID为 ： " + userId + "密码信息失败", e);
```

**优化后：**

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UserController {
    
    public void someMethod(Long userId) {
        // 使用占位符，更高效
        log.error("修改用户ID为: {} 密码信息失败", userId, e);
        
        // 日志级别规范
        log.debug("调试信息: {}", data);
        log.info("操作信息: {}", data);
        log.warn("警告信息: {}", data);
        log.error("错误信息: {}", data, e);
    }
}
```

**3. 统一异常处理增强：**

位置：`jshERP-boot/src/main/java/com/jsh/erp/exception/GlobalExceptionHandler.java`

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务参数异常
     */
    @ExceptionHandler(value = BusinessParamCheckingException.class)
    @ResponseBody
    public Object handleBusinessParamException(BusinessParamCheckingException e, 
                                                 HttpServletRequest request) {
        log.warn("业务参数异常: url={}, code={}, msg={}", 
            request.getRequestURL(), e.getCode(), e.getMessage());
        
        JSONObject status = new JSONObject();
        status.put(ExceptionConstants.GLOBAL_RETURNS_CODE, e.getCode());
        status.put(ExceptionConstants.GLOBAL_RETURNS_DATA, e.getData());
        return status;
    }
    
    /**
     * 业务运行时异常
     */
    @ExceptionHandler(value = BusinessRunTimeException.class)
    @ResponseBody
    public Object handleBusinessRunTimeException(BusinessRunTimeException e, 
                                                   HttpServletRequest request) {
        log.warn("业务运行时异常: url={}, code={}, msg={}", 
            request.getRequestURL(), e.getCode(), e.getMessage());
        
        JSONObject status = new JSONObject();
        status.put(ExceptionConstants.GLOBAL_RETURNS_CODE, e.getCode());
        status.put(ExceptionConstants.GLOBAL_RETURNS_DATA, e.getData());
        return status;
    }
    
    /**
     * 权限异常
     */
    @ExceptionHandler(value = SecurityException.class)
    @ResponseBody
    public Object handleSecurityException(SecurityException e, 
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        log.error("安全异常: url={}, msg={}", request.getRequestURL(), e.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        JSONObject status = new JSONObject();
        status.put(ExceptionConstants.GLOBAL_RETURNS_CODE, 403);
        status.put(ExceptionConstants.GLOBAL_RETURNS_DATA, "无权限访问");
        return status;
    }
    
    /**
     * 其他系统异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: url={}, msg={}", request.getRequestURL(), e.getMessage(), e);
        
        JSONObject status = new JSONObject();
        status.put(ExceptionConstants.GLOBAL_RETURNS_CODE, 
            ExceptionConstants.SERVICE_SYSTEM_ERROR_CODE);
        status.put(ExceptionConstants.GLOBAL_RETURNS_DATA, 
            ExceptionConstants.SERVICE_SYSTEM_ERROR_MSG);
        return status;
    }
}
```

---

## 四、性能优化

### 4.1 Redis keys("*")性能问题

**问题描述：** 使用`keys("*")`遍历所有Redis键，数据量大时会阻塞Redis。

**优化前代码：**

位置：`jshERP-boot/src/main/java/com/jsh/erp/service/RedisService.java:154`

```java
public void deleteObjectByUserAndIp(Long userId, String clientIp) {
    Set<String> tokens = redisTemplate.keys("*");  // 危险！会阻塞Redis
    for (String token : tokens) {
        // ...
    }
}
```

**优化后代码：**

```java
@Slf4j
@Component
public class RedisService {
    
    /**
     * 使用SCAN命令替代KEYS，避免阻塞
     */
    public Set<String> safeKeys(String pattern) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(
                ScanOptions.scanOptions().match(pattern).count(1000).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            return keys;
        });
    }
    
    /**
     * 更好的方案：使用特定前缀存储，便于清理
     */
    private static final String SESSION_PREFIX = "session:";
    
    public void storageObjectBySession(String token, String key, Object obj) {
        // 使用特定前缀
        String sessionKey = SESSION_PREFIX + token;
        redisTemplate.opsForHash().put(sessionKey, key, obj.toString());
        redisTemplate.expire(sessionKey, 
            BusinessConstants.MAX_SESSION_IN_SECONDS, TimeUnit.SECONDS);
    }
    
    public void deleteObjectByUser(Long userId) {
        // 使用SCAN安全遍历，或考虑使用Redis Set存储用户Token列表
        // 更好的设计：额外维护 userId -> tokens 的映射
        String userTokensKey = "user:tokens:" + userId;
        Set<Object> tokens = redisTemplate.opsForSet().members(userTokensKey);
        if (tokens != null) {
            for (Object token : tokens) {
                redisTemplate.delete(SESSION_PREFIX + token);
            }
            redisTemplate.delete(userTokensKey);
        }
    }
}
```

---

### 4.2 数据库查询优化

**问题描述：** 部分代码存在N+1查询问题。

**优化前代码：**

位置：`jshERP-boot/src/main/java/com/jsh/erp/service/DepotHeadService.java:129`

```java
public List<DepotHeadVo4List> select(...) throws Exception {
    List<DepotHeadVo4List> list = depotHeadMapperEx.selectByConditionDepotHead(...);
    
    for (DepotHeadVo4List dh : list) {
        // N+1查询：循环中查询数据库
        BigDecimal finishDeposit = finishDepositMap.get(dh.getNumber());
        // ...
    }
}
```

**优化建议：**

**1. 使用批量查询替代循环查询：**

```java
public List<DepotHeadVo4List> select(...) throws Exception {
    List<DepotHeadVo4List> list = depotHeadMapperEx.selectByConditionDepotHead(...);
    
    if (list.isEmpty()) {
        return list;
    }
    
    // 提取所有ID批量查询
    List<Long> idList = list.stream()
        .map(DepotHeadVo4List::getId)
        .collect(Collectors.toList());
    List<String> numberList = list.stream()
        .map(DepotHeadVo4List::getNumber)
        .collect(Collectors.toList());
    
    // 批量查询，构建Map
    Map<String, BigDecimal> finishDepositMap = 
        getFinishDepositMapByNumberList(numberList);
    Map<Long, BigDecimal> financialBillPriceMap = 
        getFinancialBillPriceMapByBillIdList(idList);
    
    // 内存中处理
    for (DepotHeadVo4List dh : list) {
        dh.setFinishDeposit(finishDepositMap.getOrDefault(dh.getNumber(), BigDecimal.ZERO));
        // ...
    }
    
    return list;
}
```

**2. MyBatis-Plus分页优化：**

```java
// 使用PageHelper或MyBatis-Plus分页插件
public Page<DepotHeadVo4List> selectByPage(Page<DepotHeadVo4List> page, 
                                               String type, String subType) {
    return page.setRecords(
        depotHeadMapperEx.selectByPage(page, type, subType)
    );
}
```

---

## 五、代码结构优化

### 5.1 统一响应封装

**问题描述：** 代码中使用多种方式返回响应数据。

**优化方案：**

**1. 定义统一响应类：**

```java
package com.jsh.erp.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 状态码 */
    private Integer code;
    
    /** 消息 */
    private String message;
    
    /** 数据 */
    private T data;
    
    /** 时间戳 */
    private Long timestamp;
    
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
```

**2. 统一分页响应：**

```java
package com.jsh.erp.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 当前页 */
    private Integer pageNum;
    
    /** 每页大小 */
    private Integer pageSize;
    
    /** 总记录数 */
    private Long total;
    
    /** 总页数 */
    private Integer pages;
    
    /** 数据列表 */
    private List<T> rows;
    
    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, 
                                          Long total, List<T> rows) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setRows(rows);
        result.setPages((int) Math.ceil((double) total / pageSize));
        return result;
    }
}
```

**3. Controller中使用：**

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
    
    @GetMapping("/list")
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = userService.page(new Page<>(pageNum, pageSize));
        PageResult<User> pageResult = PageResult.of(
            pageNum, pageSize, page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }
    
    @PostMapping
    public Result<Boolean> save(@RequestBody User user) {
        boolean saved = userService.save(user);
        return saved ? Result.success() : Result.error("保存失败");
    }
}
```

---

## 六、前端代码优化

### 6.1 调试信息清理

**问题描述：** 前端代码中存在大量`console.log`调试信息。

**优化前代码：**

位置：`jshERP-web/src/utils/hasPermission.js`

```javascript
const hasPermission = {
    install(Vue, options) {
        Vue.directive('has', {
            inserted: (el, binding, vnode) => {
                console.log("页面权限控制----");  // 调试信息
                // ...
            }
        });
    }
};

export function filterNodePermission(el, binding, vnode) {
    console.log("流程节点页面权限--NODE--");  // 调试信息
    // ...
}

export function filterGlobalPermission(el, binding, vnode) {
    console.log("全局页面权限--Global--");  // 调试信息
    // ...
}
```

**优化后代码：**

```javascript
const hasPermission = {
    install(Vue, options) {
        Vue.directive('has', {
            inserted: (el, binding, vnode) => {
                if (!filterNodePermission(el, binding, vnode)) {
                    filterGlobalPermission(el, binding, vnode);
                }
            }
        });
    }
};

export function filterNodePermission(el, binding, vnode) {
    let permissionList = [];
    try {
        let obj = vnode.context.$props.formData;
        if (obj) {
            let bpmList = obj.permissionList;
            for (let bpm of bpmList) {
                if (bpm.type != '2') {
                    permissionList.push(bpm);
                }
            }
        } else {
            return false;
        }
    } catch (e) {
        return false;
    }
    // ...
}

export function filterGlobalPermission(el, binding, vnode) {
    let permissionList = [];
    let allPermissionList = [];
    
    let authList = JSON.parse(sessionStorage.getItem(USER_AUTH) || "[]");
    for (let auth of authList) {
        if (auth.type != '2') {
            permissionList.push(auth);
        }
    }
    // ...
}
```

**可选方案：使用环境变量控制调试输出**

```javascript
// main.js
if (process.env.NODE_ENV === 'development') {
    Vue.config.debug = true;
} else {
    // 生产环境禁用console.log
    console.log = function() {};
}
```

---

### 6.2 前端请求封装优化

**优化前代码：**

位置：`jshERP-web/src/utils/request.js`

```javascript
const err = (error) => {
    if (error.response) {
        let data = error.response.data
        const token = Vue.ls.get(ACCESS_TOKEN)
        switch (error.response.status) {
            case 403:
                notification.error({ message: '系统提示', description: '拒绝访问', duration: 4})
                break
            // ...
        }
    }
    return Promise.reject(error)
}
```

**优化后代码：**

```javascript
import Vue from 'vue'
import axios from 'axios'
import store from '@/store'
import { VueAxios } from './axios'
import { Modal, notification } from 'ant-design-vue'
import { ACCESS_TOKEN } from '@/store/mutation-types'

const service = axios.create({
    baseURL: process.env.VUE_APP_API_BASE_URL || '/jshERP-boot',
    timeout: 300000,
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = Vue.ls.get(ACCESS_TOKEN)
        if (token) {
            config.headers['X-Access-Token'] = token
        }
        
        // 添加CSRF Token
        const csrfToken = Vue.ls.get('CSRF-Token')
        if (csrfToken) {
            config.headers['X-CSRF-Token'] = csrfToken
        }
        
        // 添加请求时间戳，防止缓存
        if (config.method === 'get') {
            config.params = {
                _t: Date.now(),
                ...config.params
            }
        }
        
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        
        // 处理登录失效
        if (res === 'loginOut') {
            Modal.error({
                title: '登录已过期',
                content: '很抱歉，登录已过期，请重新登录',
                okText: '重新登录',
                mask: false,
                onOk: () => {
                    store.dispatch('Logout').then(() => {
                        location.reload()
                    })
                }
            })
            return Promise.reject(new Error('登录已过期'))
        }
        
        // 处理业务错误码
        if (res.code && res.code !== 200) {
            notification.error({
                message: '系统提示',
                description: res.message || res.data || '请求失败',
                duration: 4
            })
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        
        return res
    },
    error => {
        let message = '请求失败'
        
        if (error.response) {
            const status = error.response.status
            switch (status) {
                case 400:
                    message = '请求参数错误'
                    break
                case 401:
                    message = '未授权，请重新登录'
                    store.dispatch('Logout').then(() => {
                        setTimeout(() => {
                            location.reload()
                        }, 1500)
                    })
                    break
                case 403:
                    message = '拒绝访问'
                    break
                case 404:
                    message = '请求地址不存在'
                    break
                case 408:
                    message = '请求超时'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                case 502:
                    message = '网关错误'
                    break
                case 503:
                    message = '服务不可用'
                    break
                case 504:
                    message = '网关超时'
                    break
                default:
                    message = `请求错误 [${status}]`
            }
        } else if (error.message) {
            if (error.message.includes('timeout')) {
                message = '请求超时'
            } else if (error.message.includes('Network Error')) {
                message = '网络错误，请检查网络连接'
            }
        }
        
        notification.error({
            message: '系统提示',
            description: message,
            duration: 4
        })
        
        return Promise.reject(error)
    }
)

const installer = {
    vm: {},
    install(Vue, router = {}) {
        Vue.use(VueAxios, router, service)
    }
}

export {
    installer as VueAxios,
    service as axios
}
```

---

## 七、生产环境配置优化

### 7.1 Swagger接口暴露问题

**问题描述：** 生产环境Swagger接口文档暴露，存在安全风险。

**优化方案：**

**1. 根据环境条件启用Swagger：**

```java
package com.jsh.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile({"dev", "test"})  // 只在开发和测试环境启用
public class Swagger2Config {
    
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.jsh.erp.controller"))
            .paths(PathSelectors.any())
            .build();
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("管伊佳ERP接口文档")
            .description("管伊佳ERP系统RESTful API接口文档")
            .version("3.6.0")
            .build();
    }
}
```

**2. 生产环境添加安全配置：**

```java
package com.jsh.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("prod")  // 只在生产环境启用
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（如果使用Token认证）
            .csrf().disable()
            
            // 限制Swagger访问
            .authorizeRequests()
            .antMatchers("/doc.html", "/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs")
            .denyAll()
            
            // 其他请求放行
            .anyRequest().permitAll();
        
        return http.build();
    }
}
```

---

## 八、优化建议总结

### 优先级排序

| 优先级 | 优化项 | 影响范围 | 实施难度 |
|--------|--------|----------|----------|
| 🔴 P0 | 密码加密方式升级（MD5→BCrypt） | 安全 | 中 |
| 🔴 P0 | API接口权限验证（AOP切面） | 安全 | 高 |
| 🔴 P0 | 第三方库版本升级 | 安全 | 低 |
| 🟠 P1 | CSRF防护实现 | 安全 | 中 |
| 🟠 P1 | 登录失败锁定机制 | 安全 | 低 |
| 🟠 P1 | SimpleDateFormat线程安全问题 | 性能/稳定性 | 低 |
| 🟠 P1 | Redis keys("*")优化 | 性能 | 中 |
| 🟡 P2 | XSS输入过滤 | 安全 | 中 |
| 🟡 P2 | 安全响应头添加 | 安全 | 低 |
| 🟡 P2 | 统一响应封装 | 代码质量 | 中 |
| 🟡 P2 | 日志规范统一 | 可维护性 | 低 |
| 🟢 P3 | 前端调试信息清理 | 代码质量 | 低 |
| 🟢 P3 | 生产环境Swagger禁用 | 安全 | 低 |
| 🟢 P3 | 代码规范检查工具配置 | 代码质量 | 低 |

### 建议实施顺序

1. **第一阶段（紧急修复）：**
   - 升级所有存在已知漏洞的第三方依赖
   - 实现API接口级权限验证
   - 升级密码加密方式

2. **第二阶段（安全增强）：**
   - 实现CSRF防护
   - 添加安全响应头
   - 实现登录失败锁定
   - 生产环境禁用Swagger

3. **第三阶段（代码质量）：**
   - 修复SimpleDateFormat线程安全问题
   - 统一响应封装
   - 日志规范统一
   - 前端调试信息清理

4. **第四阶段（性能优化）：**
   - 优化Redis keys操作
   - 数据库查询N+1问题优化
   - 添加代码规范检查工具

---

**报告生成时间：** 2026-04-18

**分析依据：** 代码库全量分析
