package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    private final String SECRET_KEY;
    private final long EXPIRATION_TIME;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtServiceImpl.class);

    public JwtServiceImpl(@Value("${jwt.secret-key}") String SECRET_KEY, @Value("${jwt.expiration-time}") long EXPIRATION_TIME) {
        this.SECRET_KEY = SECRET_KEY;
        this.EXPIRATION_TIME = EXPIRATION_TIME;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        LOGGER.debug("Generating token with claims: {} for user: {}", claims, userDetails.getUsername());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Claims extractClaims(String token) {
        LOGGER.debug("Extracting claims from token");
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String extractLogin(String token) {
        String login = extractClaims(token).getSubject();
        LOGGER.debug("Extracted login: {} from token", login);
        return login;
    }

    @Override
    public Date extractExpiration(String token) {
        Date expiration = extractClaims(token).getExpiration();
        LOGGER.debug("Extracted expiration: {} from token", expiration);
        return expiration;
    }

    @Override
    public boolean isValid(UserDetails user, String token) {
        boolean isValid = user.getUsername().equals(extractLogin(token)) && new Date().before(extractExpiration(token));
        LOGGER.debug("Token validation for user {}: {}", user.getUsername(), isValid);
        return isValid;
    }

    @Override
    public Key getSecretKey() {
        byte[] decode = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(decode);
    }

}
