package com.ocean.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置 — DTO 层缓存策略
 *
 * 关键：自定义 ObjectMapper（注册 JSR310 模块），解决 LocalDateTime 序列化问题
 */
@Configuration
@EnableCaching
public class CachingConfig {

    private static final Map<String, Duration> CACHE_TTLS = Map.of(
            "ocean:latest",    Duration.ofMinutes(5),
            "ocean:list",      Duration.ofMinutes(3),
            "ocean:page",      Duration.ofMinutes(3),
            "ocean:trend",     Duration.ofMinutes(5),
            "knowledge:page",  Duration.ofMinutes(30),
            "monitor:list",    Duration.ofHours(1),
            "alert:active",    Duration.ofMinutes(1)
    );

    /**
     * 构建支持 JSR310 (LocalDateTime) 的 JSON 序列化器
     */
    private static GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 不要求实体有 @JsonTypeInfo，GenericJackson2Json 会自动处理类型
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .entryTtl(Duration.ofHours(1));

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        CACHE_TTLS.forEach((name, ttl) ->
                configs.put(name, defaultConfig.entryTtl(ttl)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
