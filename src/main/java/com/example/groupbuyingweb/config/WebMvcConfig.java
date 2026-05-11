package com.example.groupbuyingweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /uploads/파일명.jpg 로 접근하면
        registry.addResourceHandler("/uploads/**")
                // 실제로는 내 PC의 특정 폴더에서 파일을 찾아 반환한다
                .addResourceLocations("file:" + uploadDir);
    }
}