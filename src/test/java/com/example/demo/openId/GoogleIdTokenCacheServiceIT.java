package com.example.demo.openId;

import com.example.demo.BaseIT;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleIdTokenCacheServiceIT extends BaseIT {

    @Autowired
    private GoogleIdTokenCacheService googleIdTokenCacheService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        Cache cache = cacheManager.getCache("openid");
        assertNotNull(cache);
        cache.clear();
    }

    @Test
    public void getJwkSet_returnJwkSetAndCacheCorrectly() {
        Cache cache = cacheManager.getCache("openid");
        assertNotNull(cache);

        long start = System.currentTimeMillis();
        JWKSet firstCall = googleIdTokenCacheService.getJwkSet();
        long firstDuration = System.currentTimeMillis() - start;
        assertNotNull(firstCall);

        Cache.ValueWrapper jwkset = cache.get("jwkset");
        assertNotNull(jwkset);
        assertNotNull(jwkset.get());

        start = System.currentTimeMillis();
        JWKSet secondCall = googleIdTokenCacheService.getJwkSet();
        long secondDuration = System.currentTimeMillis() - start;

        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);
        assertTrue(secondDuration < firstDuration / 2, "Get from cache");
    }
}
