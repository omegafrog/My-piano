package com.omegafrog.My.piano.app;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
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

	private int CACHE_BATCH_SIZE = 1000;

	@Bean
	public RedisConnectionFactory commonUserRedisConnectionFactory() {
		return new LettuceConnectionFactory(host, port);
	}

	@Bean(name = "cacheConnectionFactory")
	public RedisConnectionFactory cacheConnectionFactory() {
		return new LettuceConnectionFactory(cacheHost, cachePort);
	}

	@Qualifier("CommonUserRedisTemplate")
	@Bean
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(commonUserRedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	// @Bean
	// public RedisCacheManager cacheManager() {
	//     StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
	//     return RedisCacheManager.builder()
	//             .cacheWriter(RedisCacheWriter.lockingRedisCacheWriter(cacheConnectionFactory(), BatchStrategies.scan(CACHE_BATCH_SIZE)))
	//             .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10L)))
	//             .build();
	// }
	@Bean
	public RedisCacheManager cacheManager() {
		return RedisCacheManager.builder(cacheConnectionFactory())
			.cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
				.serializeValuesWith(RedisSerializationContext.SerializationPair
					.fromSerializer(new GenericJackson2JsonRedisSerializer().configure(
						objectMapper -> objectMapper.registerModule(
							new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()
						)
					))))
			.build();
	}
	
}
