package com.example.demo.auth;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private AuthenticationRequest authenticationRequest;

    private String token;

    private User user;

    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

    @BeforeEach
    public void setup() {
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        authenticationRequest = AuthenticationRequest.builder()
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .build();

        user = User.builder()
                .firstname("Ivan")
                .lastname("IvanIvanov")
                .login(authenticationRequest.getLogin())
                .password(authenticationRequest.getPassword())
                .role(Role.ROLE_USER)
                .build();

        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationRequest.getLogin(),
                authenticationRequest.getPassword()
        );
    }

    @Test
    public void authenticate_returnAuthResponse() {
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
        when(jwtService.generateToken(user)).thenReturn(token);

        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);

        assertNotNull(authenticationResponse);
        assertEquals(token, authenticationResponse.getToken());

        verify(authenticationManager, times(1)).authenticate(usernamePasswordAuthenticationToken);
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    public void authenticate_authExc_throwExc() {
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(authenticationRequest));

        verify(authenticationManager, times(1)).authenticate(usernamePasswordAuthenticationToken);
        verifyNoInteractions(jwtService);
    }

    @Test
    public void authenticate_userNotFound_throwExc() {
        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenThrow(new UserNotFoundException("User with login: " + authenticationRequest.getLogin() + " not found!"));

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> authenticationService.authenticate(authenticationRequest));

        assertEquals("User with login: IvanIvanov@gmail.com not found!", userNotFoundException.getMessage());
        verify(authenticationManager, times(1)).authenticate(usernamePasswordAuthenticationToken);
        verifyNoInteractions(jwtService);
    }

}