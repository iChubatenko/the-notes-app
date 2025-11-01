package com.thenotesapp.api.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI myApiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notes App")
                        .description("Notes App Documentation")
                        .version("1.0"));
    }
}
