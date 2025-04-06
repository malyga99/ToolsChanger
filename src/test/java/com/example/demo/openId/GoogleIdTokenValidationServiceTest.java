package com.example.demo.openId;

import com.example.demo.exception.OpenIdValidationException;
import com.example.demo.exception.OpenIdServiceException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleIdTokenValidationServiceTest {

    @Mock
    private IdTokenCacheService idTokenCacheService;

    @Mock
    private OpenIdProperties openIdProperties;

    @InjectMocks
    private GoogleIdTokenValidationService idTokenValidationService;

    @Test
    public void validateState_validState_doesNotThrowExc() {
        when(openIdProperties.getState()).thenReturn("validState");

        assertDoesNotThrow(() -> idTokenValidationService.validateState("validState"));
    }

    @Test
    public void validateState_invalidState_throwExc() {
        when(openIdProperties.getState()).thenReturn("validState");

        OpenIdValidationException exc = assertThrows(OpenIdValidationException.class, () -> idTokenValidationService.validateState("invalidState"));
        assertEquals("State not valid", exc.getMessage());
    }

    @Test
    void validateIdToken_validToken_shouldNotThrowException() throws JOSEException {
        String keyId = "validKeyId";

        JWSHeader jwsHeader = mock(JWSHeader.class);
        when(jwsHeader.getKeyID()).thenReturn(keyId);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getHeader()).thenReturn(jwsHeader);

        JWK jwk = mock(JWK.class);
        JWKSet jwkSet = new JWKSet(jwk);
        when(idTokenCacheService.getJwkSet()).thenReturn(jwkSet);
        when(jwk.getKeyID()).thenReturn(keyId);

        RSAKey rsaKey = mock(RSAKey.class);
        when(jwk.toRSAKey()).thenReturn(rsaKey);

        RSAPublicKey rsaPublicKey = mock(RSAPublicKey.class);
        when(rsaKey.toRSAPublicKey()).thenReturn(rsaPublicKey);

        when(signedJWT.verify(any(RSASSAVerifier.class))).thenReturn(true);

        assertDoesNotThrow(() -> idTokenValidationService.validateIdToken(signedJWT));
        verify(signedJWT, times(1)).verify(any(RSASSAVerifier.class));
        verify(idTokenCacheService, times(1)).getJwkSet();

    }

    @Test
    public void validateIdToken_missingJwkSet_throwExc() throws JOSEException {
        String keyId = "validKeyId";

        JWSHeader jwsHeader = mock(JWSHeader.class);
        when(jwsHeader.getKeyID()).thenReturn(keyId);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getHeader()).thenReturn(jwsHeader);

        when(idTokenCacheService.getJwkSet()).thenReturn(null);

        OpenIdServiceException exc = assertThrows(OpenIdServiceException.class, () -> idTokenValidationService.validateIdToken(signedJWT));
        assertEquals("JWK set is empty or it failed to load", exc.getMessage());

        verify(signedJWT, never()).verify(any(RSASSAVerifier.class));
        verify(idTokenCacheService, times(1)).getJwkSet();
    }

    @Test
    public void validateIdToken_emptyJwkSet_throwExc() throws JOSEException {
        String keyId = "validKeyId";

        JWSHeader jwsHeader = mock(JWSHeader.class);
        when(jwsHeader.getKeyID()).thenReturn(keyId);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getHeader()).thenReturn(jwsHeader);

        JWKSet jwkSet = new JWKSet();
        when(idTokenCacheService.getJwkSet()).thenReturn(jwkSet);

        OpenIdServiceException exc = assertThrows(OpenIdServiceException.class, () -> idTokenValidationService.validateIdToken(signedJWT));
        assertEquals("JWK set is empty or it failed to load", exc.getMessage());

        verify(signedJWT, never()).verify(any(RSASSAVerifier.class));
        verify(idTokenCacheService, times(1)).getJwkSet();
    }

    @Test
    public void validateIdToken_invalidKeyId_throwExc() throws JOSEException {
        JWSHeader jwsHeader = mock(JWSHeader.class);
        when(jwsHeader.getKeyID()).thenReturn("invalidKeyId");

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getHeader()).thenReturn(jwsHeader);

        JWK jwk = mock(JWK.class);
        JWKSet jwkSet = new JWKSet(jwk);
        when(idTokenCacheService.getJwkSet()).thenReturn(jwkSet);

        when(jwk.getKeyID()).thenReturn("validKeyId");

        OpenIdValidationException exc = assertThrows(OpenIdValidationException.class, () -> idTokenValidationService.validateIdToken(signedJWT));
        assertEquals("Id token not valid", exc.getMessage());
        verify(signedJWT, never()).verify(any(RSASSAVerifier.class));
        verify(idTokenCacheService, times(1)).getJwkSet();
    }

    @Test
    public void validateIdToken_invalidIdToken_throwExc() throws JOSEException {
        String keyId = "validKeyId";

        JWSHeader jwsHeader = mock(JWSHeader.class);
        when(jwsHeader.getKeyID()).thenReturn(keyId);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getHeader()).thenReturn(jwsHeader);

        JWK jwk = mock(JWK.class);
        JWKSet jwkSet = new JWKSet(jwk);
        when(idTokenCacheService.getJwkSet()).thenReturn(jwkSet);
        when(jwk.getKeyID()).thenReturn(keyId);

        RSAKey rsaKey = mock(RSAKey.class);
        when(jwk.toRSAKey()).thenReturn(rsaKey);

        RSAPublicKey rsaPublicKey = mock(RSAPublicKey.class);
        when(rsaKey.toRSAPublicKey()).thenReturn(rsaPublicKey);

        when(signedJWT.verify(any(RSASSAVerifier.class))).thenReturn(false);

        OpenIdValidationException exc = assertThrows(OpenIdValidationException.class, () -> idTokenValidationService.validateIdToken(signedJWT));
        assertEquals("Id token not valid", exc.getMessage());
        verify(signedJWT, times(1)).verify(any(RSASSAVerifier.class));
        verify(idTokenCacheService, times(1)).getJwkSet();
    }

    @Test
    public void validateIdToken_verifyFailed_throwExc() throws JOSEException {
        String keyId = "validKeyId";

        JWSHeader jwsHeader = mock(JWSHeader.class);
        when(jwsHeader.getKeyID()).thenReturn(keyId);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getHeader()).thenReturn(jwsHeader);

        JWK jwk = mock(JWK.class);
        JWKSet jwkSet = new JWKSet(jwk);
        when(idTokenCacheService.getJwkSet()).thenReturn(jwkSet);
        when(jwk.getKeyID()).thenReturn(keyId);

        RSAKey rsaKey = mock(RSAKey.class);
        when(jwk.toRSAKey()).thenReturn(rsaKey);

        RSAPublicKey rsaPublicKey = mock(RSAPublicKey.class);
        when(rsaKey.toRSAPublicKey()).thenReturn(rsaPublicKey);

        JOSEException joseException = new JOSEException("exc");
        when(signedJWT.verify(any(RSASSAVerifier.class))).thenThrow(joseException);

        OpenIdValidationException exc = assertThrows(OpenIdValidationException.class, () -> idTokenValidationService.validateIdToken(signedJWT));
        assertEquals("JWK validate exception: " + joseException.getMessage(), exc.getMessage());
        verify(signedJWT, times(1)).verify(any(RSASSAVerifier.class));
        verify(idTokenCacheService, times(1)).getJwkSet();
    }
}