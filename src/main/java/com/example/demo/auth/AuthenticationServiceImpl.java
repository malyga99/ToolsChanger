package com.example.demo.auth;

import com.example.demo.jwt.JwtService;
import com.example.demo.user.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Starting user authentication with login: {}", authenticationRequest.getLogin());

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getLogin(),
                authenticationRequest.getPassword()
        ));

        User user = (User) auth.getPrincipal();

        String token = jwtService.generateToken(user);
        long elapsedTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Successful user authentication with login: {}. Time taken: {} ms", authenticationRequest.getLogin(), elapsedTime);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}
