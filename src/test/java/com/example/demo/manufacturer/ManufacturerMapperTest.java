package com.example.demo.manufacturer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ManufacturerMapperTest {

    @InjectMocks
    private ManufacturerMapper manufacturerMapper;

    @Test
    public void toDto_returnCorrectlyDto() {
        Manufacturer manufacturer = Manufacturer.builder().id(1L).name("Makita").build();

        ManufacturerDto result = manufacturerMapper.toDto(manufacturer);

        assertNotNull(result);
        assertEquals(manufacturer.getId(), result.getId());
        assertEquals(manufacturer.getName(), result.getName());
    }

}