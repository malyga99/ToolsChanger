package com.example.demo.register;

import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.jwt.JwtService;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        long startTime = System.currentTimeMillis();
        registerRequest.setLogin(registerRequest.getLogin().toLowerCase());
        LOGGER.info("Starting user register with login: {}", registerRequest.getLogin());

        if (userRepository.existsByLogin(registerRequest.getLogin())) {
            throw new UserAlreadyExistsException("User with login: " + registerRequest.getLogin() + " already exists!");
        }

        User user = createUser(registerRequest);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        long elapsedTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Successful user register with login: {}. Time taken: {} ms", registerRequest.getLogin(), elapsedTime);

        return RegisterResponse.builder()
                .token(token)
                .build();
    }

    private User createUser(RegisterRequest registerRequest) {
        return User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .login(registerRequest.getLogin())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.ROLE_USER)
                .build();
    }
}
