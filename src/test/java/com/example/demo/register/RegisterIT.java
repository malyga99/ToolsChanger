package com.example.demo.register;

import com.example.demo.BaseIT;
import com.example.demo.exception.ResponseError;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterIT extends BaseIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private RegisterRequest registerRequest;

    @BeforeEach
    public void setup() {
        registerRequest = RegisterRequest.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("ivanivanov@gmail.com")
                .password("abcde")
                .build();
        userRepository.deleteAll();
    }

    @Test
    public void register_returnJwtToken() {
        ResponseEntity<RegisterResponse> response = testRestTemplate.postForEntity("/api/v1/register", registerRequest, RegisterResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String token = response.getBody().getToken();
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("ivanivanov@gmail.com", jwtService.extractLogin(token));

        Optional<User> savedUser = userRepository.findByLogin("ivanivanov@gmail.com");
        assertTrue(savedUser.isPresent());
        assertEquals("Ivan", savedUser.get().getFirstname());
        assertEquals("Ivanov", savedUser.get().getLastname());
        assertEquals("ivanivanov@gmail.com", savedUser.get().getLogin());
        assertTrue(passwordEncoder.matches("abcde", savedUser.get().getPassword()));
        assertEquals(Role.ROLE_USER, savedUser.get().getRole());
    }

    @Test
    public void register_userAlreadyExists_returnConflict() {
        userRepository.save(User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("ivanivanov@gmail.com")
                .password(passwordEncoder.encode("abcde"))
                .role(Role.ROLE_USER)
                .build());
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/register", registerRequest, ResponseError.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with login: ivanivanov@gmail.com already exists!", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(1, userRepository.count());
    }

    @Test
    public void register_duplicateLoginIgnoreCase_returnConflict() {
        userRepository.save(User.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("ivanivanov@gmail.com")
                .password(passwordEncoder.encode("abcde"))
                .role(Role.ROLE_USER)
                .build());

        registerRequest.setLogin("IVANIVANOV@gmail.com");
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/register", registerRequest, ResponseError.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with login: ivanivanov@gmail.com already exists!", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(1, userRepository.count());
    }

    @Test
    public void register_invalidData_returnBadRequest() {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .firstname("")
                .lastname("")
                .login("")
                .password("")
                .build();
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/register", invalidRequest, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(0, userRepository.count());
    }

    @Test
    public void register_tooLongFirstname_returnBadRequest() {
        registerRequest.setFirstname("A".repeat(101));
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/register", registerRequest, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));

        assertEquals(0, userRepository.count());

    }
}
