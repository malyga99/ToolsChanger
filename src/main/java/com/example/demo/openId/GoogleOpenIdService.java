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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class GoogleOpenIdService implements OpenIdService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final IdTokenValidationService idTokenValidationService;
    private final OpenIdProperties openIdProperties;
    private final OpenIdUserService openIdUserService;
    private final JwtService jwtService;
    private final static Logger LOGGER = LoggerFactory.getLogger(GoogleOpenIdService.class);

    @Override
    public String getJwtToken(String authCode, String state) {
        LOGGER.info("Starting receiving ID token from Google");

        idTokenValidationService.validateState(state);

        String idToken = sendRequest(authCode);

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(idToken);
        } catch (ParseException e) {
            throw new OpenIdValidationException("Failed to parse JWT: " + e.getMessage(), e);
        }
        LOGGER.debug("Successfully parsed ID token");

        idTokenValidationService.validateIdToken(signedJWT);

        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new OpenIdValidationException("Failed to extract claims from JWT: " + e.getMessage(), e);
        }
        LOGGER.debug("Successfully parsed claims from JWT");

        String login = extractClaim(jwtClaimsSet, "email");
        String firstname = extractClaim(jwtClaimsSet, "given_name");
        String lastname = extractClaim(jwtClaimsSet, "family_name");
        User user = openIdUserService.getOrCreateUser(login, firstname, lastname);
        LOGGER.info("Returning JWT token for user: {}", user.getLogin());

        return jwtService.generateToken(user);
    }

    @Override
    public String sendRequest(String authCode) {
        LOGGER.debug("Sending request to Google with authCode");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", openIdProperties.getClientId());
        body.add("client_secret", openIdProperties.getClientSecret());
        body.add("redirect_uri", openIdProperties.getRedirectUri());
        body.add("grant_type", openIdProperties.getGrantType());
        body.add("code", authCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            String errorMessage = response.getBody();
            throw new OpenIdServiceException("Failed to retrieve ID token from Google: " + errorMessage);
        }

        String responseBody = response.getBody();
        LOGGER.debug("Received response from Google");

        return extractIdToken(responseBody);

    }

    @Override
    public String extractIdToken(String responseBody) {
        LOGGER.debug("Extracting ID token from response body");

        String idToken;
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode idTokenNode = jsonNode.get("id_token");
            if (idTokenNode == null) {
                throw new OpenIdValidationException("ID token not found in the response body");
            }

            idToken = idTokenNode.asText();
        } catch (JsonProcessingException e) {
            throw new OpenIdValidationException("Exchange id token exception: " + e.getMessage(), e);
        }

        LOGGER.debug("Extracted ID token: {}", idToken);
        return idToken;

    }

    @Override
    public String extractClaim(JWTClaimsSet claimsSet, String claimName) {
        try {
            String claim = claimsSet.getClaimAsString(claimName);
            if (!claimName.equals("family_name") && claim == null) {
                throw new OpenIdValidationException("Claim: " + claimName + " not found in the JWT");
            }

            return claim;
        } catch (ParseException e) {
            throw new OpenIdValidationException("Failed to extract claim: " + claimName + " from JWT: " + e.getMessage(), e);
        }
    }


}
