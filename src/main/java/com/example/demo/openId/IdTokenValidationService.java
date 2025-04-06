package com.example.demo.openId;

import com.nimbusds.jwt.SignedJWT;

public interface IdTokenValidationService {

    void validateState(String state);

    void validateIdToken(SignedJWT signedJWT);
}
