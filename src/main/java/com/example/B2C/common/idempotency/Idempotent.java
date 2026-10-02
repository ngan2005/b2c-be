package com.example.B2C.common.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String scope();

    /**
     * SpEL expression to derive the idempotency key from the method arguments.
     * Default uses the first argument of type String annotated with header.
     */
    String keyExpression() default "";

    /**
     * Parameter index (0-based) that holds the request body used to compute request hash.
     */
    int requestArgIndex() default 0;
}
