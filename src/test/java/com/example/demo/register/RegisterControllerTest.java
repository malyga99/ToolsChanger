package com.example.demo.register;

import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.handler.GlobalHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    @Mock
    private RegisterService registerService;

    @InjectMocks
    private RegisterController registerController;

    private MockMvc mockMvc;

    private RegisterRequest registerRequest;

    private RegisterResponse registerResponse;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        registerRequest = RegisterRequest.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .build();

        registerResponse = RegisterResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .build();
    }

    @Test
    public void register_returnRegisterResponse() throws Exception {
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);
        when(registerService.register(registerRequest)).thenReturn(registerResponse);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(registerResponse.getToken()));

        verify(registerService, times(1)).register(registerRequest);
    }

    @Test
    public void register_userAlreadyExists_returnConflict() throws Exception {
        UserAlreadyExistsException userAlreadyExistsException = new UserAlreadyExistsException("User with login: " + registerRequest.getLogin() + " already exists!");
        String registerRequestJson = objectMapper.writeValueAsString(registerRequest);
        when(registerService.register(registerRequest)).thenThrow(userAlreadyExistsException);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(userAlreadyExistsException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(409));

        verify(registerService, times(1)).register(registerRequest);
    }

    @Test
    public void register_invalidData_returnBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .firstname("")
                .lastname("")
                .login("")
                .password("")
                .build();
        String registerRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(registerService);
    }

}