package com.omegafrog.My.piano.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // local-storage 디렉토리를 static resource로 매핑
        registry.addResourceHandler("/sheets/**")
                .addResourceLocations("file:./local-storage/sheets/")
                .setCachePeriod(3600);
        
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:./local-storage/thumbnails/")
                .setCachePeriod(3600);
        
        registry.addResourceHandler("/profiles/**")
                .addResourceLocations("file:./local-storage/profiles/")
                .setCachePeriod(3600);
    }
}