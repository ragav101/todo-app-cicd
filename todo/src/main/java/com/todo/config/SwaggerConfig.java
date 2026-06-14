package com.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI todoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo API")
                        .description("Todo management service — create, update, and track todos with priority and status")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Backend Team")
                                .email("backend@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local")));
    }
}
