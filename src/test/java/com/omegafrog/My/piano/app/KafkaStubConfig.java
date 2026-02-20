package com.omegafrog.My.piano.app;

import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class KafkaStubConfig {

    @Bean
    @Primary
    @SuppressWarnings({"unchecked", "rawtypes"})
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture<Object> completedFuture = CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(anyString(), any())).thenReturn((CompletableFuture) completedFuture);
        when(kafkaTemplate.send(anyString(), any(), any())).thenReturn((CompletableFuture) completedFuture);

        return kafkaTemplate;
    }
}
