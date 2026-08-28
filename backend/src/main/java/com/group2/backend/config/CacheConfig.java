package com.group2.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            build("tagSuggestions",   5,  200),
            build("popularTags",     15,  100),
            build("trendingArtworks",10,   50),
            build("accountArtworks",  5,  300),
            build("likeCounts",       2, 1000),
            build("accounts",        15,  500),
            build("allAccounts",     15,  100)
        ));
        return manager;
    }

    private CaffeineCache build(String name, int ttlMinutes, int maxSize) {
        return new CaffeineCache(name,
            Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build());
    }
}
