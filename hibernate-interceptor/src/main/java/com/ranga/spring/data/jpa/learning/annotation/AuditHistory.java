package com.ranga.spring.data.jpa.learning.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation can be placed Only on Entity which has 'id' property as default
 * or marked by 'AuditHistoryIdentifier'
 *
 * @author Ranga Bhupal
 * @version 1.0
 * @since 2020-12-29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AuditHistory {
    AuditHistoryType[] value() default AuditHistoryType.ALL;
}
