package com.example.phonebook.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // 设置缓存名称
        cacheManager.setCacheNames(Arrays.asList(
            "contacts",        // 联系人列表缓存
            "contact",         // 联系人详情缓存
            "searchResults",   // 搜索结果缓存
            "categoryStats"    // 分类统计缓存
        ));
        
        // 允许运行时创建新的缓存
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}
