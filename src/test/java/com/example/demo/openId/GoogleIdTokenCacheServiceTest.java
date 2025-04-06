package com.example.demo.openId;

import com.example.demo.exception.OpenIdServiceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleIdTokenCacheServiceTest {

    @InjectMocks
    private GoogleIdTokenCacheService googleIdTokenCacheService = new GoogleIdTokenCacheService("https://www.googleapis.com/oauth2/v3/certs");

    @Test
    public void getJwkSet_returnJwkSet() throws IOException {
        try (MockedStatic<JWKSet> mockedStatic = mockStatic(JWKSet.class)) {
            URL url = new URL("https://www.googleapis.com/oauth2/v3/certs");
            JWK jwk = mock(JWK.class);
            JWKSet jwkSet = new JWKSet(jwk);
            mockedStatic.when(() -> JWKSet.load(url)).thenReturn(jwkSet);

            JWKSet result = googleIdTokenCacheService.getJwkSet();

            assertNotNull(result);
            assertEquals(1, result.getKeys().size());
            assertSame(result.getKeys().get(0), jwk);

            mockedStatic.verify(() -> JWKSet.load(url), times(1));
        }
    }

    @Test
    public void getJwkSet_failedLoad_throwExc() throws MalformedURLException {
        try (MockedStatic<JWKSet> mockedStatic = mockStatic(JWKSet.class)) {
            URL url = new URL("https://www.googleapis.com/oauth2/v3/certs");
            IOException loadException = new IOException("Load exception");
            mockedStatic.when(() -> JWKSet.load(url)).thenThrow(loadException);

            OpenIdServiceException openIdException = assertThrows(OpenIdServiceException.class, (() -> googleIdTokenCacheService.getJwkSet()));
            assertEquals("Jwk set load exception: " + loadException.getMessage(), openIdException.getMessage());

            mockedStatic.verify(() -> JWKSet.load(url), times(1));
        }
    }

    @Test
    public void getJwkSet_failedParse_throwExc() throws MalformedURLException {
        try (MockedStatic<JWKSet> mockedStatic = mockStatic(JWKSet.class)) {
            URL url = new URL("https://www.googleapis.com/oauth2/v3/certs");
            ParseException parseException = new ParseException("Parse exception", 0);
            mockedStatic.when(() -> JWKSet.load(url)).thenThrow(parseException);

            OpenIdServiceException openIdException = assertThrows(OpenIdServiceException.class, (() -> googleIdTokenCacheService.getJwkSet()));
            assertEquals("Jwk set load exception: " + parseException.getMessage(), openIdException.getMessage());

            mockedStatic.verify(() -> JWKSet.load(url), times(1));
        }
    }

}