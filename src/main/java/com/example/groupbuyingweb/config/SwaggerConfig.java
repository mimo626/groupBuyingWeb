package com.example.groupbuyingweb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("도토리 창고 API")
                        .description("공동구매 서비스 도토리 창고의 REST API 문서입니다.")
                        .version("v1.0.0"));
    }
}
