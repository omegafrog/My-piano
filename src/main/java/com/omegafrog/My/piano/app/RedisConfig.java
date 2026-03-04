package com.omegafrog.My.piano.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

	@Value("${spring.redis.user.host}")
	private String host;

	@Value("${spring.redis.cache.host}")
	private String cacheHost;

	@Value("${spring.redis.user.port}")
	private int port;
	@Value("${spring.redis.cache.port}")
	private int cachePort;

	@Bean
	public RedisConnectionFactory commonUserRedisConnectionFactory() {
		return new LettuceConnectionFactory(host, port);
	}

	@Bean(name = "cacheConnectionFactory")
	public RedisConnectionFactory cacheConnectionFactory() {
		return new LettuceConnectionFactory(cacheHost, cachePort);
	}

	@Bean(name = "CommonUserRedisTemplate")
	public RedisTemplate<String, String> commonUserRedisTemplate() {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(commonUserRedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

}
