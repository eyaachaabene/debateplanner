package com.defense.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/**
 * General gateway bean configuration.
 */
@Configuration
public class GatewayConfig {

    /**
     * Shared AntPathMatcher used by filters for public-route matching.
     */
    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
