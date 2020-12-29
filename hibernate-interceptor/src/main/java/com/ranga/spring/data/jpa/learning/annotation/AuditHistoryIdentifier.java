package com.ranga.spring.data.jpa.learning.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation helps to identify Entity
 *
 * @author Ranga Bhupal
 * @version 1.0
 * @since 2020-12-29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AuditHistoryIdentifier {

}