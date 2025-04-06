package com.example.demo.auth;

import com.example.demo.BaseIT;
import com.example.demo.exception.ResponseError;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationIT extends BaseIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    public void setup() {
        authenticationRequest = AuthenticationRequest.builder()
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .build();
        userRepository.deleteAll();
    }

    @Test
    public void authenticate_returnJwtToken() {
        userRepository.save(User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password(passwordEncoder.encode("abcde"))
                .role(Role.ROLE_USER)
                .build());
        ResponseEntity<AuthenticationResponse> response = testRestTemplate.postForEntity("/api/v1/auth", authenticationRequest, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String token = response.getBody().getToken();
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("IvanIvanov@gmail.com", jwtService.extractLogin(token));
    }

    @Test
    public void authenticate_wrongPassword_returnUnauthorized() {
        userRepository.save(User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password(passwordEncoder.encode("incorrectPassword"))
                .role(Role.ROLE_USER)
                .build());
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/auth", authenticationRequest, ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad credentials", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void authenticate_userNotFound_returnUnauthorized() {
        userRepository.save(User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("NotIvan@gmail.com")
                .password(passwordEncoder.encode("abcde"))
                .role(Role.ROLE_USER)
                .build());
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/auth", authenticationRequest, ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with login: IvanIvanov@gmail.com not found!", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void authenticate_invalidData_returnBadRequest() {
        AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                .login("")
                .password("")
                .build();
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/auth", invalidRequest, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }
}
