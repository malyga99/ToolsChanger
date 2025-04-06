package com.example.demo.openId;

import com.nimbusds.jwt.JWTClaimsSet;

public interface OpenIdService {

    String getJwtToken(String authCode, String state);

    String sendRequest(String authCode);

    String extractIdToken(String responseBody);

    String extractClaim(JWTClaimsSet claimsSet, String claimName);
}
