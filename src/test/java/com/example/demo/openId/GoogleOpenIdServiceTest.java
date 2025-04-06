package com.example.demo.openId;

import com.example.demo.exception.OpenIdServiceException;
import com.example.demo.exception.OpenIdValidationException;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOpenIdServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OpenIdProperties openIdProperties;

    @Mock
    private IdTokenValidationService idTokenValidationService;

    @Mock
    private OpenIdUserService openIdUserService;

    @Mock
    private JwtService jwtService;

    @Spy
    @InjectMocks
    private GoogleOpenIdService openIdService;

    @Test
    public void getJwtToken_returnJwtToken() throws ParseException {
        try (MockedStatic<SignedJWT> signedJWTMockedStatic = mockStatic(SignedJWT.class)) {
            String authCode = "mockAuthCode";
            String state = "mockState";
            String idToken = "mockIdToken";
            String login = "mockLogin";
            String firstname = "mockFirstname";
            String lastname = "mockLastname";
            String jwtToken = "mockJwtToken";
            SignedJWT signedJWT = mock(SignedJWT.class);
            User user = User.builder()
                    .login("mockLogin")
                    .firstname("mockFirstname")
                    .lastname("mockLastname")
                    .build();
            JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);

            doNothing().when(idTokenValidationService).validateState(state);
            doNothing().when(idTokenValidationService).validateIdToken(signedJWT);
            doReturn(idToken).when(openIdService).sendRequest(authCode);

            signedJWTMockedStatic.when(() -> SignedJWT.parse(idToken)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);
            when(jwtClaimsSet.getClaimAsString("email")).thenReturn(login);
            when(jwtClaimsSet.getClaimAsString("given_name")).thenReturn(firstname);
            when(jwtClaimsSet.getClaimAsString("family_name")).thenReturn(lastname);

            when(openIdUserService.getOrCreateUser(login, firstname, lastname)).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn(jwtToken);

            String result = openIdService.getJwtToken(authCode, state);

            assertNotNull(result);
            assertEquals(jwtToken, result);

            verify(idTokenValidationService, times(1)).validateState(state);
            verify(idTokenValidationService, times(1)).validateIdToken(signedJWT);
            verify(openIdService, times(1)).sendRequest(authCode);
            verify(signedJWT, times(1)).getJWTClaimsSet();
            verify(jwtService, times(1)).generateToken(user);
            verify(jwtClaimsSet, times(3)).getClaimAsString(any(String.class));
            signedJWTMockedStatic.verify(() -> SignedJWT.parse(idToken), times(1));
        }

    }

    @Test
    public void getJwtToken_failedParseJwt_throwExc() {
        try (MockedStatic<SignedJWT> signedJWTMockedStatic = mockStatic(SignedJWT.class)) {
            String authCode = "mockAuthCode";
            String state = "mockState";
            String idToken = "mockIdToken";
            ParseException parseException = new ParseException("Parse exception", 0);

            doNothing().when(idTokenValidationService).validateState(state);
            doReturn(idToken).when(openIdService).sendRequest(authCode);
            signedJWTMockedStatic.when(() -> SignedJWT.parse(idToken)).thenThrow(parseException);

            OpenIdValidationException openIdException = assertThrows(OpenIdValidationException.class, () -> openIdService.getJwtToken(authCode, state));
            assertEquals("Failed to parse JWT: " + parseException.getMessage(), openIdException.getMessage());

            verify(idTokenValidationService, times(1)).validateState(state);
            verify(openIdService, times(1)).sendRequest(authCode);
            signedJWTMockedStatic.verify(() -> SignedJWT.parse(idToken), times(1));
        }
    }

    @Test
    public void getJwtToken_failedParseJwtClaims_throwExc() throws ParseException {
        try (MockedStatic<SignedJWT> signedJWTMockedStatic = mockStatic(SignedJWT.class)) {
            String authCode = "mockAuthCode";
            String state = "mockState";
            String idToken = "mockIdToken";
            SignedJWT signedJWT = mock(SignedJWT.class);
            ParseException parseException = new ParseException("Parse exception", 0);

            doNothing().when(idTokenValidationService).validateState(state);
            doNothing().when(idTokenValidationService).validateIdToken(signedJWT);
            doReturn(idToken).when(openIdService).sendRequest(authCode);

            signedJWTMockedStatic.when(() -> SignedJWT.parse(idToken)).thenReturn(signedJWT);
            when(signedJWT.getJWTClaimsSet()).thenThrow(parseException);

            OpenIdValidationException openIdException = assertThrows(OpenIdValidationException.class, () -> openIdService.getJwtToken(authCode, state));
            assertEquals("Failed to extract claims from JWT: " + parseException.getMessage(), openIdException.getMessage());

            verify(idTokenValidationService, times(1)).validateState(state);
            verify(idTokenValidationService, times(1)).validateIdToken(signedJWT);
            verify(openIdService, times(1)).sendRequest(authCode);
            signedJWTMockedStatic.verify(() -> SignedJWT.parse(idToken), times(1));
            verify(signedJWT, times(1)).getJWTClaimsSet();
        }
    }

    @Test
    public void sendRequest_buildHttpRequestCorrectly()  {
        String expectedResponseBody = "{\"id_token\": \"mockIdToken\"}";
        when(openIdProperties.getClientId()).thenReturn("mockClientId");
        when(openIdProperties.getClientSecret()).thenReturn("mockClientSecret");
        when(openIdProperties.getRedirectUri()).thenReturn("mockRedirectUri");
        when(openIdProperties.getGrantType()).thenReturn("mockGrantType");
        doReturn("mockIdToken").when(openIdService).extractIdToken(expectedResponseBody);

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        openIdService.sendRequest("authCode");

        verify(restTemplate, times(1)).exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                argumentCaptor.capture(),
                eq(String.class)
        );

        HttpEntity<MultiValueMap<String, String>> httpEntity = argumentCaptor.getValue();
        MultiValueMap<String, String> body = httpEntity.getBody();
        HttpHeaders headers = httpEntity.getHeaders();

        assertEquals("mockClientId", body.getFirst("client_id"));
        assertEquals("mockClientSecret", body.getFirst("client_secret"));
        assertEquals("mockRedirectUri", body.getFirst("redirect_uri"));
        assertEquals("mockGrantType", body.getFirst("grant_type"));
        assertEquals("authCode", body.getFirst("code"));
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
    }

    @Test
    public void sendRequest_returnIdToken() {
        String expectedResponseBody = "{\"id_token\": \"mockIdToken\"}";
        doReturn("mockIdToken").when(openIdService).extractIdToken(expectedResponseBody);

        ResponseEntity<String> response = new ResponseEntity<>(expectedResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);


        String result = openIdService.sendRequest("authCode");

        assertNotNull(result);
        assertEquals("mockIdToken", result);

        verify(restTemplate, times(1)).exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(openIdService, times(1)).extractIdToken(expectedResponseBody);
    }

    @Test
    public void sendRequest_badResponse_throwExc() {
        String errorResponseBody = "Error";
        ResponseEntity<String> response = new ResponseEntity<>(errorResponseBody, HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        OpenIdServiceException openIdException = assertThrows(OpenIdServiceException.class, () -> openIdService.sendRequest("authCode"));
        assertEquals("Failed to retrieve ID token from Google: " + errorResponseBody, openIdException.getMessage());
    }

    @Test
    public void extractIdToken_returnIdToken() throws JsonProcessingException {
        String responseBody = "{\"id_token\": \"idToken\"}";
        String expectedIdToken = "idToken";
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode jsonNodeIdToken = mock(JsonNode.class);
        when(objectMapper.readTree(responseBody)).thenReturn(jsonNode);
        when(jsonNode.get("id_token")).thenReturn(jsonNodeIdToken);
        when(jsonNodeIdToken.asText()).thenReturn(expectedIdToken);

        String idToken = openIdService.extractIdToken(responseBody);

        assertNotNull(idToken);
        assertEquals(expectedIdToken, idToken);

        verify(objectMapper, times(1)).readTree(responseBody);
        verify(jsonNode, times(1)).get("id_token");
        verify(jsonNodeIdToken, times(1)).asText();
    }

    @Test
    public void extractIdToken_withoutIdToken_throwExc() throws JsonProcessingException {
        String responseBody = "{\"id_token\": \"idToken\"}";
        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(responseBody)).thenReturn(jsonNode);
        when(jsonNode.get("id_token")).thenReturn(null);

        OpenIdValidationException openIdException = assertThrows(OpenIdValidationException.class, () -> openIdService.extractIdToken(responseBody));
        assertEquals("ID token not found in the response body", openIdException.getMessage());

        verify(objectMapper, times(1)).readTree(responseBody);
        verify(jsonNode, times(1)).get("id_token");
    }

    @Test
    public void extractIdToken_invalidJson_throwExc() throws JsonProcessingException {
        String responseBody = "{\"id_token\": \"idToken\"}";
        when(objectMapper.readTree(responseBody)).thenThrow(JsonProcessingException.class);

        OpenIdValidationException openIdException = assertThrows(OpenIdValidationException.class, () -> openIdService.extractIdToken(responseBody));
        assertTrue(openIdException.getMessage().contains("Exchange id token exception: "));

        verify(objectMapper, times(1)).readTree(responseBody);
    }

    @Test
    public void extractClaim_returnExtractedClaim() throws ParseException {
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        when(jwtClaimsSet.getClaimAsString("email")).thenReturn("IvanIvanov@gmail.com");
        when(jwtClaimsSet.getClaimAsString("given_name")).thenReturn("Ivan");
        when(jwtClaimsSet.getClaimAsString("family_name")).thenReturn("Ivanov");

        String email = openIdService.extractClaim(jwtClaimsSet, "email");
        String givenName = openIdService.extractClaim(jwtClaimsSet, "given_name");
        String familyName = openIdService.extractClaim(jwtClaimsSet, "family_name");

        assertNotNull(email);
        assertNotNull(givenName);
        assertNotNull(familyName);
        assertEquals("IvanIvanov@gmail.com", email);
        assertEquals("Ivan", givenName);
        assertEquals("Ivanov", familyName);
    }

    @Test
    public void extractClaim_withoutEmail_throwExc() throws ParseException {
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        when(jwtClaimsSet.getClaimAsString("email")).thenReturn(null);

        OpenIdValidationException openIdValidationException = assertThrows(OpenIdValidationException.class, () -> openIdService.extractClaim(jwtClaimsSet, "email"));
        assertEquals("Claim: email not found in the JWT", openIdValidationException.getMessage());
    }

    @Test
    public void extractClaim_withoutGivenName_throwExc() throws ParseException {
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        when(jwtClaimsSet.getClaimAsString("given_name")).thenReturn(null);

        OpenIdValidationException openIdValidationException = assertThrows(OpenIdValidationException.class, () -> openIdService.extractClaim(jwtClaimsSet, "given_name"));
        assertEquals("Claim: given_name not found in the JWT", openIdValidationException.getMessage());
    }

    @Test
    public void extractClaim_withoutFamilyName_doesNotThrowExc() throws ParseException {
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        when(jwtClaimsSet.getClaimAsString("family_name")).thenReturn(null);

        assertDoesNotThrow(() -> openIdService.extractClaim(jwtClaimsSet, "family_name"));
    }

    @Test
    public void extractClaim_ifFailed_throwExc() throws ParseException {
        JWTClaimsSet jwtClaimsSet = mock(JWTClaimsSet.class);
        when(jwtClaimsSet.getClaimAsString("email")).thenThrow(new ParseException("Parse failed", 0));

        OpenIdValidationException openIdValidationException = assertThrows(OpenIdValidationException.class, () -> openIdService.extractClaim(jwtClaimsSet, "email"));
        assertEquals("Failed to extract claim: email from JWT: Parse failed", openIdValidationException.getMessage());
    }

}