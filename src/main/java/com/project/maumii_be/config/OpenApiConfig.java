package com.project.maumii_be.config;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@Tag(name = "Maumii", description = "마음이 API")  // Swagger UI에서 보여질 태그
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("마음이(Maumii) API")  // API 제목
                        .description("마음이 서비스를 위한 REST API")  // API 설명
                        .version("1.0.0")  // API 버전
                        .contact(new Contact()  // 연락처 정보
                                .name("인사이드아웃")
                                .email("jyj2385@naver.com")));
    }
}