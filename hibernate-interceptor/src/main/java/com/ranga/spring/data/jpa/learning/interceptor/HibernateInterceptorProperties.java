package com.ranga.spring.data.jpa.learning.interceptor;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ranga Bhupal
 * @version 1.0
 * @since 2020-12-29
 */
@Data
@Component
@ConfigurationProperties(prefix = "audit.history")
public class HibernateInterceptorProperties {
    @Value("${newFormat: New '%s' of '%s' Id}")
    String newFormat;

    @Value("${deleteFormat: Deleted '%s' of '%s' Id}")
    String deleteFormat;

    @Value("${modifiedFormat: - '%s' changed from '%s' to '%s'}")
    String modifiedFormat;
}
