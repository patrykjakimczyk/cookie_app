package com.cookie.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public record ConfigProperties(
        String frontendAddress,
        String jwtSecret,
        String[] ignoreMatchers
) {}
