package com.omegafrog.My.piano.app;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path storageBasePath;

    public WebConfig(@Value("${local.storage.base-path}") String storageBasePath) {
        this.storageBasePath = Path.of(storageBasePath).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/sheets/**")
                .addResourceLocations(resourceLocation("sheets"))
                .setCachePeriod(3600);

        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations(resourceLocation("thumbnails"))
                .setCachePeriod(3600);

        registry.addResourceHandler("/profiles/**")
                .addResourceLocations(resourceLocation("profiles"))
                .setCachePeriod(3600);
    }

    private String resourceLocation(String directory) {
        String location = storageBasePath.resolve(directory).toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
