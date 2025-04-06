package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public interface JwtService {

     Claims extractClaims(String token);

     String extractLogin(String token);

     Date extractExpiration(String token);

     Key getSecretKey();

     boolean isValid(UserDetails user, String token);

     String generateToken(Map<String, Object> claims, UserDetails userDetails);

     String generateToken(UserDetails userDetails);
}
