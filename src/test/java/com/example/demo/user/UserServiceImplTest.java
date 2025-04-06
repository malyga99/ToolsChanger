package com.example.demo.user;

import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Authentication authentication;

    private SecurityContext securityContext;

    private User user;

    @BeforeEach
    public void setup() {
        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
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
    public void getCurrentUser_returnCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("IvanIvanov@gmail.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByLogin("IvanIvanov@gmail.com")).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(user, result);

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByLogin("IvanIvanov@gmail.com");
    }

    @Test
    public void getCurrentUser_userNotFound_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("IvanIvanov@gmail.com");
        when(userRepository.findByLogin("IvanIvanov@gmail.com")).thenReturn(Optional.empty());
        when(authentication.isAuthenticated()).thenReturn(true);

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser());
        assertEquals("User with this login: IvanIvanov@gmail.com not found", userNotFoundException.getMessage());

        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByLogin("IvanIvanov@gmail.com");
    }

    @Test
    public void getCurrentUser_withoutAuthentication_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(null);

        AuthenticationException authenticationException = assertThrows(AuthenticationException.class, () -> userService.getCurrentUser());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());

        verifyNoInteractions(userRepository, authentication);
    }

    @Test
    public void getCurrentUser_notAuthenticated_throwExc() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        AuthenticationException authenticationException = assertThrows(AuthenticationException.class, () -> userService.getCurrentUser());
        assertEquals("Current user is not authenticated", authenticationException.getMessage());

        verify(authentication, times(1)).isAuthenticated();
        verifyNoInteractions(userRepository);
    }

    @Test
    public void findById_returnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(user, result);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void findById_userNotFound_throwExc() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
        assertEquals("User with id: 1 not found", userNotFoundException.getMessage());

        verify(userRepository, times(1)).findById(1L);
    }

}