//// src/main/java/com/project/maumii_be/config/WebConfig.java
//package com.project.maumii_be.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.*;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry reg) {
//        // /media/** 로 들어오면 실제 uploads 폴더를 내보냄
//        reg.addResourceHandler("/media/**")
//                .addResourceLocations("file:uploads/")
//                .setCachePeriod(0);
//
//        reg.addResourceHandler("/voices/**")
//                .addResourceLocations("file:uploads/voices/")
//                .setCachePeriod(0);
//    }
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins(
//                        "http://localhost:5050",
//                        "https://192.168.230.9:5173",
//                        "https://maumii-43895739287.us-central1.run.app"
//                )
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(false);
//    }
//}