package com.example.demo.openId;

import com.example.demo.exception.OpenIdValidationException;
import com.example.demo.handler.GlobalHandler;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenIdControllerTest {

    @Mock
    private OpenIdService openIdService;

    @InjectMocks
    private OpenIdController openIdController;

    private MockMvc mockMvc;

    private String token;

    private String authCode;

    private String state;

    @BeforeEach
    public void setup() {
        authCode = "authCode";
        state = "state";
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        mockMvc = MockMvcBuilders.standaloneSetup(openIdController)
                .setControllerAdvice(new GlobalHandler())
                .build();
    }

    @Test
    public void getJwtToken_returnJwtToken() throws Exception {
        when(openIdService.getJwtToken(authCode, state)).thenReturn(token);

        mockMvc.perform(post("/api/v1/openid")
                        .param("authCode", authCode)
                        .param("state", state)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(token));

        verify(openIdService, times(1)).getJwtToken(authCode, state);
    }

    @Test
    public void getJwtToken_notValid_throwExc() throws Exception {
        when(openIdService.getJwtToken(authCode, state)).thenThrow(new OpenIdValidationException("Validation exception"));

        mockMvc.perform(post("/api/v1/openid")
                        .param("authCode", authCode)
                        .param("state", state)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation exception"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.time").exists());

        verify(openIdService, times(1)).getJwtToken(authCode, state);
    }

    @Test
    public void getJwtToken_missingAuthCode_returnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/openid")
                        .param("state", state)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getJwtToken_missingState_returnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/openid")
                        .param("authCode", authCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}