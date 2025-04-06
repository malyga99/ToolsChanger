package com.example.demo.jwt;

import com.example.demo.user.Role;
import com.example.demo.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService = new JwtServiceImpl("934091AE98362741F722202EED3288E8FF2509C73641ADBF75EEB3195A926B40", 100000);

    private UserDetails userDetails;

    @BeforeEach
    public void setup() {
        userDetails = User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    public void generateToken_returnCorrectlyToken() {
        String token = jwtService.generateToken(userDetails);
        Claims claims = jwtService.extractClaims(token);

        assertNotNull(token);
        assertEquals(userDetails.getUsername(), claims.getSubject());
        assertTrue(new Date().before(claims.getExpiration()));
    }

    @Test
    public void generateToken_withExtraClaims_setCorrectlyExtraClaims() {
        String token = jwtService.generateToken(Map.of(
                "first_claim", "value_first_claim",
                "second_claim", "value_second_claim"
        ), userDetails);

        Claims claims = jwtService.extractClaims(token);

        assertNotNull(token);
        assertEquals("value_first_claim", claims.get("first_claim"));
        assertEquals("value_second_claim", claims.get("second_claim"));
    }

    @Test
    public void extractLogin_returnCorrectlyLogin() {
        String token = jwtService.generateToken(userDetails);
        String login = jwtService.extractLogin(token);

        assertNotNull(login);
        assertEquals(userDetails.getUsername(), login);
    }

    @Test
    public void extractExpiration_returnExpiration() {
        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(new Date().before(expiration));
    }

    @Test
    public void isValid_validToken_returnTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean result = jwtService.isValid(userDetails, token);

        assertTrue(result);
    }

    @Test
    public void isValid_invalidLogin_returnFalse() {
        UserDetails user = User.builder()
                .login("PetrPetrov")
                .build();
        String token = jwtService.generateToken(userDetails);
        boolean result = jwtService.isValid(user, token);

        assertFalse(result);
    }

    @Test
    public void isValid_expiredToken_throwExc() {
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(jwtService.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.isValid(userDetails, token));
    }


    @Test
    public void getSecretKey_returnSecretKey() {
        Key secretKey = jwtService.getSecretKey();

        assertNotNull(secretKey);
    }


}