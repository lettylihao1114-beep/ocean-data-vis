package com.ocean.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存总开关
 * - 有 Redis 时 → RedisCacheManager 自动接管
 * - 无 Redis 时 → Spring 默认 ConcurrentMapCacheManager（内存缓存）
 */
@Configuration
@EnableCaching
public class CachingConfig {
}
