package com.jsh.erp.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    String moduleName();

    String operationType() default "";

    boolean logParams() default true;

    boolean logResult() default false;

    String contentExpression() default "";
}
