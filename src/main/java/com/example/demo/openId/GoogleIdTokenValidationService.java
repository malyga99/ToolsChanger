package com.example.demo.openId;

import com.example.demo.exception.OpenIdValidationException;
import com.example.demo.exception.OpenIdServiceException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleIdTokenValidationService implements IdTokenValidationService {

    private final OpenIdProperties openIdProperties;
    private final IdTokenCacheService googleIdTokenCacheService;
    private final static Logger LOGGER = LoggerFactory.getLogger(GoogleIdTokenValidationService.class);

    @Override
    public void validateState(String state) {
        LOGGER.debug("Starting state validation");
        boolean isValid = state.equals(openIdProperties.getState());
        if (!isValid) {
            throw new OpenIdValidationException("State not valid");
        }

        LOGGER.debug("Successfully state validation");
    }

    @Override
    public void validateIdToken(SignedJWT signedJWT) {
        LOGGER.debug("Starting id token validation");
        boolean isValid = false;
        String keyID = signedJWT.getHeader().getKeyID();

        JWKSet jwkSet = googleIdTokenCacheService.getJwkSet();

        if (jwkSet == null || jwkSet.getKeys().isEmpty()) {
            throw new OpenIdServiceException("JWK set is empty or it failed to load");
        }

        for (JWK jwk : jwkSet.getKeys()) {
            if (jwk.getKeyID().equals(keyID)) {
                try {
                    RSASSAVerifier rsassaVerifier = new RSASSAVerifier(jwk.toRSAKey());
                    isValid = signedJWT.verify(rsassaVerifier);
                } catch (JOSEException e) {
                    throw new OpenIdValidationException("JWK validate exception: " + e.getMessage(), e);
                }
            }
        }

        if (!isValid) {
            throw new OpenIdValidationException("Id token not valid");
        }

        LOGGER.debug("Successfully ID token validation");
    }
}
