package com.omegafrog.My.piano.app.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("local-seed")
public class LocalContentSeedConfiguration {

    @Bean
    LocalContentSeeder localContentSeeder(
            UserRepository userRepository,
            PostRepository postRepository,
            SheetPostRepository sheetPostRepository,
            CommentRepository commentRepository,
            ObjectMapper objectMapper,
            @Value("${local.storage.base-path}") String storageBasePath,
            @Value("${local.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        return new LocalContentSeeder(
                userRepository,
                postRepository,
                sheetPostRepository,
                commentRepository,
                objectMapper,
                storageBasePath,
                publicBaseUrl);
    }

    @Bean
    CommandLineRunner localContentSeedRunner(LocalContentSeeder seeder) {
        return arguments -> seeder.seed();
    }
}
