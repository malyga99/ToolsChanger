package com.example.demo.openId;

import com.example.demo.BaseIT;
import com.example.demo.exception.ResponseError;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public class OpenIdIT extends BaseIT {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private IdTokenCacheService idTokenCacheService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private JWKSet mockJwkSet;

    private RSAKey mockRsaKey;

    private String mockIdToken;

    @BeforeEach
    public void setup() throws JOSEException {
        userRepository.deleteAll();
        mockJwkSet = generateMockJwkSet();
        mockRsaKey = (RSAKey) mockJwkSet.getKeys().get(0);
        mockIdToken = generateValidJwt(mockRsaKey);
    }

    @Test
    public void getJwtTokenOpenId_returnJwtToken() throws JOSEException {
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + mockIdToken + "\"}");
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(idTokenCacheService.getJwkSet()).thenReturn(mockJwkSet);

        ResponseEntity<String> response = testRestTemplate.postForEntity(url, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isBlank());
        assertEquals("IvanIvanov@gmail.com", jwtService.extractLogin(response.getBody()));

        Optional<User> savedUser = userRepository.findByLogin("IvanIvanov@gmail.com");
        assertTrue(savedUser.isPresent());
        assertEquals("Ivan", savedUser.get().getFirstname());
        assertEquals("Ivanov", savedUser.get().getLastname());
        assertEquals("IvanIvanov@gmail.com", savedUser.get().getLogin());
        assertNull(savedUser.get().getPassword());
        assertEquals(Role.ROLE_USER, savedUser.get().getRole());
    }

    @Test
    public void getJwtTokenOpenId_userAlreadyExists_returnJwtTokenAndDoesNotCreateUser()  {
        userRepository.save(User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password(passwordEncoder.encode("abcde"))
                .role(Role.ROLE_USER)
                .build());
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + mockIdToken + "\"}");
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(idTokenCacheService.getJwkSet()).thenReturn(mockJwkSet);

        ResponseEntity<String> response = testRestTemplate.postForEntity(url, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isBlank());
        assertEquals(3, response.getBody().split("\\.").length);
        assertEquals("IvanIvanov@gmail.com", jwtService.extractLogin(response.getBody()));

        assertEquals(1, userRepository.count());
    }

    @Test
    public void getJwtTokenOpenId_invalidState_returnBadRequest() {
        String authCode = "mockAuthCode";
        String state = "invalidState";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity(url, null, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("State not valid", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(0, userRepository.count());
    }

    @Test
    public void getJwtTokenOpenId_emptyJwkSet_returnInternalServerError() throws JOSEException {
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + mockIdToken + "\"}");
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(idTokenCacheService.getJwkSet()).thenReturn(new JWKSet());

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity(url, null, ResponseError.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JWK set is empty or it failed to load", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(0, userRepository.count());
    }

    @Test
    public void getJwtTokenOpenId_jwkSetIsNull_returnInternalServerError() throws JOSEException {
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + mockIdToken + "\"}");
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(idTokenCacheService.getJwkSet()).thenReturn(null);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity(url, null, ResponseError.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JWK set is empty or it failed to load", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(0, userRepository.count());
    }

    @Test
    public void getJwtTokenOpenId_invalidKeyId_returnBadRequest() throws JOSEException {
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);
        RSAKey wrongRsaKey = new RSAKeyGenerator(2048).keyID("wrong-key-id").generate();
        String invalidIdToken = generateValidJwt(wrongRsaKey);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + invalidIdToken + "\"}");
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(idTokenCacheService.getJwkSet()).thenReturn(mockJwkSet);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity(url, null, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Id token not valid", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(0, userRepository.count());

    }

    @Test
    public void getJwtTokenOpenId_googleReturnsError_returnInternalServerError() {
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);

        ResponseEntity<String> mockErrorResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"invalid_grant\", \"error_description\":\"Bad request\"}");

        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockErrorResponse);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity(url, null, ResponseError.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Failed to retrieve ID token from Google: {\"error\":\"invalid_grant\", \"error_description\":\"Bad request\"}", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void getJwtTokenOpenId_withoutClaim_returnBadRequest() throws JOSEException {
        String authCode = "mockAuthCode";
        String state = "a2FsZmZsd2xmd2x3Zmx3ZmFhbGZ3bGZ3YWxmd2Fsd2FmbHdmYWw";
        String url = String.format("/api/v1/openid?authCode=%s&state=%s", authCode, state);
        String idTokenWithoutClaim = generateValidJwtWithoutClaim(mockRsaKey);

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id_token\": \"" + idTokenWithoutClaim + "\"}");
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);
        when(idTokenCacheService.getJwkSet()).thenReturn(mockJwkSet);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity(url, null, ResponseError.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Claim: email not found in the JWT", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    private JWKSet generateMockJwkSet() throws JOSEException {
        RSAKey mockRsaKey = new RSAKeyGenerator(2048)
                .keyID("test-key-id")
                .generate();

        return new JWKSet(mockRsaKey);
    }

    private String generateValidJwt(RSAKey mockRsaKey) throws JOSEException {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject("1234567890")
                .issuer("https://accounts.google.com")
                .claim("email", "IvanIvanov@gmail.com")
                .claim("given_name", "Ivan")
                .claim("family_name", "Ivanov")
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 * 10))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(mockRsaKey.getKeyID()).build(),
                jwtClaimsSet
        );

        signedJWT.sign(new RSASSASigner(mockRsaKey));

        return signedJWT.serialize();
    }

    private String generateValidJwtWithoutClaim(RSAKey mockRsaKey) throws JOSEException {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject("1234567890")
                .issuer("https://accounts.google.com")
                .claim("given_name", "Ivan")
                .claim("family_name", "Ivanov")
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 * 10))
                .build();


        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(mockRsaKey.getKeyID()).build(),
                jwtClaimsSet
        );

        signedJWT.sign(new RSASSASigner(mockRsaKey));

        return signedJWT.serialize();
    }
}



