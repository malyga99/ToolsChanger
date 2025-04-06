package com.example.demo.auth;

import com.example.demo.exception.UserNotFoundException;
import com.example.demo.handler.GlobalHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;

    private AuthenticationRequest authenticationRequest;

    private AuthenticationResponse authenticationResponse;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .build();

        authenticationResponse = AuthenticationResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .build();
    }

    @Test
    public void authenticate_returnAuthResponse() throws Exception {
        String authRequestJson = objectMapper.writeValueAsString(authenticationRequest);
        when(authenticationService.authenticate(authenticationRequest)).thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(authenticationResponse.getToken()));

        verify(authenticationService, times(1)).authenticate(authenticationRequest);
    }

    @Test
    public void authenticate_userNotFound_returnNotFound() throws Exception {
        String authRequestJson = objectMapper.writeValueAsString(authenticationRequest);
        UserNotFoundException userNotFoundException = new UserNotFoundException("User with login: " + authenticationRequest.getLogin() + " not found!");
        when(authenticationService.authenticate(authenticationRequest)).thenThrow(userNotFoundException);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(userNotFoundException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(404));

        verify(authenticationService, times(1)).authenticate(authenticationRequest);
    }

    @Test
    public void authenticate_invalidData_returnBadRequest() throws Exception {
        AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                .login("")
                .password("")
                .build();
        String authRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(authenticationService);
    }

}