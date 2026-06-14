package com.master.config;

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
    public OpenAPI masterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Master API")
                        .description("Reference data service — manages Categories and SubCategories")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Backend Team")
                                .email("backend@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")));
    }
}
