package com.example.demo.openId;

import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OpenIdUserService {

    private final UserRepository userRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(OpenIdUserService.class);


    public User getOrCreateUser(String login, String firstname, String lastname) {
        Optional<User> userFromDb = userRepository.findByLogin(login);
        User user = userFromDb.orElseGet(() ->
        {
            LOGGER.debug("Creating new OpenID user with login: {}", login);
            return userRepository.save(User.builder()
                    .login(login)
                    .firstname(firstname)
                    .lastname(lastname)
                    .role(Role.ROLE_USER)
                    .build());
        });

        return user;
    }
}
