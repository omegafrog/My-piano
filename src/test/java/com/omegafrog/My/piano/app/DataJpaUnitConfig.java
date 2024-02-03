package com.omegafrog.My.piano.app;

import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.security.infrastructure.redis.RedisRefreshTokenRepository;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrderRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.Subscription.RedisSubscriptionRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ComponentScan(basePackages = {"com.omegafrog.My.piano.app.web.infra", "com.omegafrog.My.piano.app.web.domain.cash"})
@EnableRedisRepositories(basePackages = {"com.omegafrog.My.piano.app.security", "com.omegafrog.My.piano.app.web.infra.Subscription"},
        includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {RedisSubscriptionRepository.class})})
@EnableJpaRepositories(excludeFilters =
        {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {RedisSubscriptionRepository.class, SheetPostIndexRepository.class, RedisRefreshTokenRepository.class})})

public class DataJpaUnitConfig {
    @Value("${spring.redis.user.host}")
    private String host;

    @Value("${spring.redis.user.port}")
    private int port;
    @Value("${spring.redis.admin.host}")
    private String adminHost;

    @Value("${spring.redis.admin.port}")
    private int adminPort;

    @Bean
    public RedisConnectionFactory commonUserRedisConnectionFactory(){
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisConnectionFactory adminRedisConnectionFactory(){
        return new LettuceConnectionFactory(adminHost, adminPort);
    }
    @Qualifier("CommonUserRedisTemplate")
    @Bean
    public RedisTemplate<?, ?> redisTemplate(){
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(commonUserRedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Qualifier("AdminRedisTemplate")
    @Bean
    public RedisTemplate<?, ?> adminRedisTemplate(){
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(adminRedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
