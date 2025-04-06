package com.example.demo.manufacturer;

import com.example.demo.BaseIT;
import com.example.demo.exception.ManufacturerNotFoundException;
import com.example.demo.exception.ResponseError;
import com.example.demo.register.RegisterRequest;
import com.example.demo.register.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ManufacturerIT extends BaseIT {

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ManufacturerService manufacturerService;

    @BeforeEach
    public void setup() {
        manufacturerRepository.deleteAll();
    }

    @Test
    public void findAll_returnAllManufacturers() {
        initDataSql();

        String token = registerAndGetToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<ManufacturerDto>> response = testRestTemplate.exchange("/api/v1/manufacturers",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<ManufacturerDto> result = response.getBody();
        assertEquals(3, result.size());
        assertEquals("Bosch", result.get(0).getName());
        assertEquals("Makita", result.get(1).getName());
        assertEquals("DeWalt", result.get(2).getName());
    }

    @Test
    public void findAll_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/manufacturers",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void findById_returnManufacturerById() {
        initDataSql();
        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();

        Manufacturer result = manufacturerService.findById(manufacturerId);

        assertNotNull(result);
        assertEquals("Bosch", result.getName());
    }

    @Test
    public void findById_manufacturerNotFound_returnNotFound() {
        initDataSql();
        Long manufacturerId = 999L;

        ManufacturerNotFoundException manufacturerNotFoundException = assertThrows(ManufacturerNotFoundException.class, () -> manufacturerService.findById(manufacturerId));
        assertEquals("Manufacturer with id: 999 not found", manufacturerNotFoundException.getMessage());
    }

    private String registerAndGetToken() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .login("IvanIvanov@gmail.com")
                .password("abcde")
                .build();
        ResponseEntity<RegisterResponse> response = testRestTemplate.postForEntity("/api/v1/register", registerRequest, RegisterResponse.class);

        return "Bearer " + response.getBody().getToken();
    }

    private void initDataSql() {
        List<Manufacturer> manufacturers = List.of(
                Manufacturer.builder().name("Bosch").build(),
                Manufacturer.builder().name("Makita").build(),
                Manufacturer.builder().name("DeWalt").build());

        manufacturerRepository.saveAll(manufacturers);
    }
}
