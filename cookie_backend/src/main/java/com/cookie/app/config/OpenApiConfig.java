package com.cookie.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "cookie_app",
                description = "Documentation for cookie_app",
                version = "1.0"
        ),
        servers =  {
                @Server(url = "http://localhost:8081"),
                @Server(url = "http://cookie-backend:8080")
        }
)
@SecurityScheme(
        name = "basicAuth",
        description = "Insert your credentials",
        scheme = "basic",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Insert JWT token",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
