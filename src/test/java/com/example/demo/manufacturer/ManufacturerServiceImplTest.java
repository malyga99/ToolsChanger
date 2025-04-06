package com.example.demo.manufacturer;

import com.example.demo.exception.ManufacturerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceImplTest {

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ManufacturerMapper manufacturerMapper;

    @InjectMocks
    private ManufacturerServiceImpl manufacturerService;

    private ManufacturerDto firstManufacturerDto;

    private ManufacturerDto secondManufacturerDto;

    private Manufacturer firstManufacturer;

    private Manufacturer secondManufacturer;

    @BeforeEach
    public void setup() {
        firstManufacturerDto = ManufacturerDto.builder()
                .id(1L)
                .name("Makita")
                .build();
        secondManufacturerDto = ManufacturerDto.builder()
                .id(2L)
                .name("Bosch")
                .build();
        firstManufacturer = Manufacturer.builder()
                .id(1L)
                .name("Makita")
                .build();
        secondManufacturer = Manufacturer.builder()
                .id(2L)
                .name("Bosh")
                .build();
    }

    @Test
    public void findAll_returnTwoManufacturers() {
        when(manufacturerRepository.findAll()).thenReturn(List.of(firstManufacturer, secondManufacturer));
        when(manufacturerMapper.toDto(firstManufacturer)).thenReturn(firstManufacturerDto);
        when(manufacturerMapper.toDto(secondManufacturer)).thenReturn(secondManufacturerDto);

        List<ManufacturerDto> result = manufacturerService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(firstManufacturerDto, result.get(0));
        assertEquals(secondManufacturerDto, result.get(1));
    }

    @Test
    public void findById_returnManufacturer() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(firstManufacturer));

        Manufacturer result = manufacturerService.findById(1L);

        assertNotNull(result);
        assertEquals(firstManufacturer, result);

        verify(manufacturerRepository, times(1)).findById(1L);
    }

    @Test
    public void findById_manufacturerNotFound_throwExc() {
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        ManufacturerNotFoundException manufacturerNotFoundException = assertThrows(ManufacturerNotFoundException.class, () -> manufacturerService.findById(1L));

        assertEquals("Manufacturer with id: 1 not found", manufacturerNotFoundException.getMessage());

        verify(manufacturerRepository, times(1)).findById(1L);
    }

}