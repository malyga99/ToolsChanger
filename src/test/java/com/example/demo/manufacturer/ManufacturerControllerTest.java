package com.example.demo.manufacturer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ManufacturerControllerTest {

    @Mock
    private ManufacturerService manufacturerService;

    @InjectMocks
    private ManufacturerController manufacturerController;

    private ManufacturerDto firstManufacturerDto;

    private ManufacturerDto secondManufacturerDto;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(manufacturerController).build();
        firstManufacturerDto = ManufacturerDto.builder()
                .id(1L)
                .name("Makita")
                .build();
        secondManufacturerDto = ManufacturerDto.builder()
                .id(2L)
                .name("Bosch")
                .build();
    }

    @Test
    public void findAll_returnTwoManufacturers() throws Exception {
        when(manufacturerService.findAll()).thenReturn(List.of(firstManufacturerDto, secondManufacturerDto));

        mockMvc.perform(get("/api/v1/manufacturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Makita"))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].name").value("Bosch"));

        verify(manufacturerService, times(1)).findAll();
    }
}