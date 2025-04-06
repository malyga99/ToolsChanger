package com.example.demo.register;

import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private RegisterRequest registerRequest;

    private String token;

    @BeforeEach
    public void setup() {
        registerRequest = RegisterRequest.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("ivanivanov@gmail.com")
                .password("abcde")
                .build();

        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    }

    @Test
    public void register_savesUserCorrectly() {
        when(userRepository.existsByLogin(registerRequest.getLogin())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        registerService.register(registerRequest);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository, times(1)).existsByLogin(registerRequest.getLogin());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        assertEquals(registerRequest.getFirstname(), savedUser.getFirstname());
        assertEquals(registerRequest.getLastname(), savedUser.getLastname());
        assertEquals(registerRequest.getLogin(), savedUser.getLogin());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    public void register_returnRegisterResponse() {
        registerRequest.setLogin(registerRequest.getLogin().toLowerCase());
        when(userRepository.existsByLogin(registerRequest.getLogin())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn(token);

        RegisterResponse registerResponse = registerService.register(registerRequest);

        verify(userRepository, times(1)).existsByLogin(registerRequest.getLogin());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(userRepository, times(1)).save(any(User.class));

        assertNotNull(registerResponse);
        assertEquals(token, registerResponse.getToken());
    }

    @Test
    public void register_userAlreadyExists_throwExc() {
        registerRequest.setLogin(registerRequest.getLogin().toLowerCase());
        when(userRepository.existsByLogin(registerRequest.getLogin())).thenReturn(true);

        UserAlreadyExistsException userAlreadyExistsException = assertThrows(UserAlreadyExistsException.class, () -> registerService.register(registerRequest));
        assertEquals("User with login: ivanivanov@gmail.com already exists!", userAlreadyExistsException.getMessage());

        verifyNoInteractions(jwtService, passwordEncoder);
        verify(userRepository, times(1)).existsByLogin(registerRequest.getLogin());
    }
}