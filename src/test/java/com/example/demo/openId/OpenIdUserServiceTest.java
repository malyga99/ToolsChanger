package com.example.demo.openId;

import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenIdUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OpenIdUserService openIdUserService;

    private User user;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    public void getOrCreateUser_ifExist_returnExistingUser() {
        when(userRepository.findByLogin("IvanIvanov@gmail.com")).thenReturn(Optional.of(user));

        User result = openIdUserService.getOrCreateUser("IvanIvanov@gmail.com", "Ivan", "Ivanov");

        assertNotNull(result);
        assertEquals(user, result);

        verify(userRepository, times(1)).findByLogin("IvanIvanov@gmail.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void getOrCreateUser_ifDoesNotExist_createUser() {
        when(userRepository.findByLogin("IvanIvanov@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = openIdUserService.getOrCreateUser("IvanIvanov@gmail.com", "Ivan", "Ivanov");

        assertNotNull(result);
        assertEquals(user, result);

        verify(userRepository, times(1)).findByLogin("IvanIvanov@gmail.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

}