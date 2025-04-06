package com.example.demo.openId;

import com.example.demo.exception.OpenIdServiceException;
import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

@Service
@CacheConfig(cacheNames = "openid")
public class GoogleIdTokenCacheService implements IdTokenCacheService {

    private final String GOOGLE_CERTS_URL;
    private final static Logger LOGGER = LoggerFactory.getLogger(GoogleIdTokenCacheService.class);

    public GoogleIdTokenCacheService(@Value("${openid.google.certs-url}") String GOOGLE_CERTS_URL) {
        this.GOOGLE_CERTS_URL = GOOGLE_CERTS_URL;
    }

    @Cacheable(key = "'jwkset'")
    public JWKSet getJwkSet() {
        LOGGER.debug("Attempting to load JWK set from Google endpoint: {}", GOOGLE_CERTS_URL);
        try {
            JWKSet jwkSet = JWKSet.load(new URL(GOOGLE_CERTS_URL));
            LOGGER.debug("Successfully loaded JWK set from Google endpoint");
            return jwkSet;
        } catch (IOException | ParseException e) {
            throw new OpenIdServiceException("Jwk set load exception: " + e.getMessage(), e);
        }
    }
}
