package com.interviewplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI 3.0 configuration.
 *
 * <p>Swagger UI available at: {@code /swagger-ui.html} (dev profile only)</p>
 * <p>Raw spec available at:   {@code /api-docs}</p>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:ai-mock-interview-platform}")
    private String appName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Current server")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("AI Mock Interview Platform API")
                .version("1.0.0")
                .description("""
                        Production-grade audio-first AI Mock Interview and Assessment Platform.
                        
                        This API powers the candidate-facing interview sessions, assessment pipeline,
                        report generation, and analytics features.
                        
                        **Sprint 1 Note:** Only the /health endpoint is implemented. Full API
                        surface will be available in subsequent sprints.
                        """)
                .contact(new Contact()
                        .name("AI Mock Interview Platform — Engineering Team")
                        .email("engineering@interviewplatform.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://interviewplatform.com/license"));
    }
}
