package com.example.demo.category;

import com.example.demo.BaseIT;
import com.example.demo.exception.CategoryNotFoundException;
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

public class CategoryIT extends BaseIT {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CategoryService categoryService;

    @BeforeEach
    public void setup() {
        categoryRepository.deleteAll();
    }

    @Test
    public void findAll_returnAllCategories() {
        initDataSql();

        String token = registerAndGetToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<CategoryDto>> response = testRestTemplate.exchange("/api/v1/categories",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<CategoryDto> result = response.getBody();
        assertEquals(3, result.size());
        assertEquals("Drill", result.get(0).getName());
        assertEquals("Hammer", result.get(1).getName());
        assertEquals("Screwdriver", result.get(2).getName());
    }

    @Test
    public void findAll_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/categories",
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
    public void findById_returnCategoryById() {
        initDataSql();
        Long categoryId = categoryRepository.findAll().get(0).getId();

        Category result = categoryService.findById(categoryId);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
    }

    @Test
    public void findById_categoryNotFound_returnNotFound() {
        initDataSql();
        Long categoryId = 999L;

        CategoryNotFoundException categoryNotFoundException = assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(categoryId));
        assertEquals("Category with id: 999 not found", categoryNotFoundException.getMessage());
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
        List<Category> categories = List.of(
                Category.builder().name("Drill").build(),
                Category.builder().name("Hammer").build(),
                Category.builder().name("Screwdriver").build());

        categoryRepository.saveAll(categories);
    }
}
