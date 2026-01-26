package com.sep490.anomaly_training_backend.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Authorization header using Bearer scheme.  Example: \"Bearer {token}\""
)
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Anomaly Training API")
                        .version("1.0.0")
                        .description("""
                                API documentation for Anomaly Training System. 
                                
                                ## Authentication
                                - Most endpoints require JWT Bearer token
                                - Get token via `/api/auth/login` or OAuth2 Microsoft login
                                
                                ## Roles
                                - **MANAGER**: Full access, final approval
                                - **SUPERVISOR**:  Approve/reject from Team Leaders
                                - **TEAM_LEADER**: Create plans, reports, results
                                - **FINAL_INSPECTION**: Confirm training results
                                """)
                        .contact(new Contact()
                                .name("IT Team")
                                .email("it-support@sep490.com"))
                        .license(new License()
                                .name("Internal Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development"),
                        new Server().url("https://api.sep490-training.com").description("Production")
                ));
    }
}
