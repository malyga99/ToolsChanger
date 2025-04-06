package com.example.demo.openId;

import com.nimbusds.jose.jwk.JWKSet;

public interface IdTokenCacheService {
    JWKSet getJwkSet();
}
