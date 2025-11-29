package com.projector.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI projectorOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Projector API")
                                .description("Универсальная система управления проектами")
                                .version("0.0.1-SNAPSHOT")
                                .contact(new Contact().name("Projector Team"))
                                .license(new License().name("Proprietary")))
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:" + serverPort)
                                        .description("Local development server"),
                                new Server()
                                        .url("https://api.projector.local")
                                        .description("Development server"),
                                new Server()
                                        .url("https://api.projector.prod")
                                        .description("Production server")));
    }
}
