package com.example.demo.jwt;

import com.example.demo.user.Role;
import com.example.demo.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_withoutAuthHeader_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    public void doFilterInternal_notJwtToken_skipFilter() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Notbearer jwt");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    public void doFilterInternal_withoutLogin_skipFilter() throws ServletException, IOException {
        String token = "my.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractLogin(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        verify(jwtService, times(1)).extractLogin(token);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    public void doFilterInternal_userAlreadyAuthenticated_skipFilter() throws ServletException, IOException {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        String token = "my.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractLogin(token)).thenReturn("IvanIvanov@gmail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        verify(jwtService, times(1)).extractLogin(token);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    public void doFilterInternal_notValidToken_skipFilter() throws ServletException, IOException {
        UserDetails user = User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .role(Role.ROLE_USER)
                .build();
        String token = "my.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractLogin(token)).thenReturn("IvanIvanov@gmail.com");
        when(userDetailsService.loadUserByUsername("IvanIvanov@gmail.com")).thenReturn(user);
        when(jwtService.isValid(user, token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        verify(jwtService, times(1)).extractLogin(token);
        verify(jwtService, times(1)).isValid(user, token);
        verify(userDetailsService, times(1)).loadUserByUsername("IvanIvanov@gmail.com");
    }

    @Test
    public void doFilterInternal_validToken_setAuthentication() throws ServletException, IOException {
        UserDetails user = User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .role(Role.ROLE_USER)
                .build();
        String token = "my.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractLogin(token)).thenReturn("IvanIvanov@gmail.com");
        when(userDetailsService.loadUserByUsername("IvanIvanov@gmail.com")).thenReturn(user);
        when(jwtService.isValid(user, token)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), user.getUsername());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        verify(jwtService, times(1)).extractLogin(token);
        verify(jwtService, times(1)).isValid(user, token);
        verify(userDetailsService, times(1)).loadUserByUsername("IvanIvanov@gmail.com");
    }
}