package com.example.demo.jwt;

import com.example.demo.exception.ResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Test
    public void commence_throwExc() throws IOException {
        AuthenticationException authenticationException = new AuthenticationException("Authentication Exception") {};
        PrintWriter printWriter = mock(PrintWriter.class);
        ArgumentCaptor<ResponseError> argumentCaptor = ArgumentCaptor.forClass(ResponseError.class);

        when(response.getWriter()).thenReturn(printWriter);
        when(objectMapper.writeValueAsString(any(ResponseError.class))).thenReturn("Json");

        authenticationEntryPoint.commence(request, response, authenticationException);

        verify(response, times(1)).setContentType("application/json");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(objectMapper, times(1)).writeValueAsString(argumentCaptor.capture());
        verify(printWriter, times(1)).write("Json");

        ResponseError responseError = argumentCaptor.getValue();
        assertNotNull(responseError);
        assertEquals(responseError.getMessage(), authenticationException.getMessage());
        assertEquals(responseError.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);
        assertNotNull(responseError.getTime());
    }

}