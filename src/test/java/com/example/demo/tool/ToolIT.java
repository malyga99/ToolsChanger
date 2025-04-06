package com.example.demo.tool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.example.demo.BaseIT;
import com.example.demo.PaginatedResponse;
import com.example.demo.auth.AuthenticationRequest;
import com.example.demo.auth.AuthenticationResponse;
import com.example.demo.category.Category;
import com.example.demo.category.CategoryRepository;
import com.example.demo.elasticsearch.*;
import com.example.demo.exception.ElasticsearchException;
import com.example.demo.exception.MinIoException;
import com.example.demo.exception.ResponseError;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerRepository;
import com.example.demo.minio.MinioInitializer;
import com.example.demo.minio.MinioProperties;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ToolIT extends BaseIT {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ToolDocumentMapper toolDocumentMapper;

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private ElasticProperties elasticProperties;

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticInitializer elasticInitializer;

    @Autowired
    private MinioInitializer minioInitializer;

    private final List<String> photos = List.of("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg", "photo5.jpg");

    @BeforeEach
    public void setup() {
        cleanElastic();
        cleanMinio();
        cleanSql();
    }

    @Test
    public void findAll_returnAllTools() {
        initDataSql();
        initDataMinio();
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange("/api/v1/tools?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(5, response.getBody().getTotalElements());
        assertEquals(5, response.getBody().getSize());
        assertEquals(1, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Drill 1", result.get(0).getDescription());
        assertEquals("Saw 2", result.get(1).getDescription());
        assertEquals("Hammer 3", result.get(2).getDescription());
        assertEquals("Wrench 4", result.get(3).getDescription());
        assertEquals("Screwdriver 5", result.get(4).getDescription());

        result.forEach(tool -> {
            assertNotNull(tool.getPhotos());
            tool.getPhotos().forEach(photo ->
                    assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio correctly created presigned urls")
            );
        });
    }

    @Test
    public void findAll_withPageSizeThree_returnThreeElementsPerPage() {
        initDataSql();
        initDataMinio();
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange("/api/v1/tools?pageNumber=0&pageSize=3",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(5, response.getBody().getTotalElements());
        assertEquals(3, response.getBody().getSize());
        assertEquals(2, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Drill 1", result.get(0).getDescription());
        assertEquals("Saw 2", result.get(1).getDescription());
        assertEquals("Hammer 3", result.get(2).getDescription());

        result.forEach(tool -> {
            assertNotNull(tool.getPhotos());
            tool.getPhotos().forEach(photo ->
                    assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio correctly created presigned urls")
            );
        });
    }

    @Test
    public void findAll_withNoTools_returnEmptyList() {
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange("/api/v1/tools?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void findAll_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                null,
                ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void findMy_returnUserTools() {
        initDataSql();
        initDataMinio();
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange("/api/v1/tools/my?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(5, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Drill 1", result.get(0).getDescription());
        assertEquals("Saw 2", result.get(1).getDescription());
        assertEquals("Hammer 3", result.get(2).getDescription());
        assertEquals("Wrench 4", result.get(3).getDescription());
        assertEquals("Screwdriver 5", result.get(4).getDescription());

        result.forEach(tool -> {
            assertNotNull(tool.getPhotos());
            tool.getPhotos().forEach(photo ->
                    assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio create presigned urls correctly")
            );
        });
    }

    @Test
    public void findMy_withNoTools_returnEmptyList() {
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange("/api/v1/tools/my?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void findMy_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/my?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                null,
                ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void findById_returnToolById() {
        initDataSql();
        initDataMinio();
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        Long toolId = toolRepository.findAll().get(0).getId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ToolDto> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ToolDto tool = response.getBody();
        assertEquals("Drill 1", tool.getDescription());

        assertNotNull(tool.getPhotos());
        tool.getPhotos().forEach(photo ->
                assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio create presigned urls correctly"));
    }

    @Test
    public void findById_toolNotFound_returnNotFound() {
        initDataSql();
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        Long toolId = 999L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tool with id: %d not found".formatted(toolId), response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void create_createsTool() {
        initDataSql();
        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Created description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "created file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "created.jpg";
            }
        });
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ToolDto> response = testRestTemplate.postForEntity("/api/v1/tools",
                request,
                ToolDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        ToolDto savedTool = response.getBody();
        assertNotNull(savedTool.getId());
        assertEquals(manufacturerId, savedTool.getManufacturer().getId());
        assertEquals(categoryId, savedTool.getCategory().getId());
        assertEquals(Type.RENT, savedTool.getType());
        assertEquals(Condition.NEW, savedTool.getCondition());
        assertEquals(new BigDecimal("3000"), savedTool.getPrice());
        assertEquals("Created description", savedTool.getDescription());

        assertNotNull(savedTool.getPhotos());
        savedTool.getPhotos().forEach(photo ->
                assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio create presigned urls correctly"));

        Optional<Tool> toolFromDb = toolRepository.findById(savedTool.getId());
        assertTrue(toolFromDb.isPresent());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 1); // Wait until document appears in ES
        assertTrue(isDocumentExistsInElastic(savedTool.getId()), "Elasticsearch create document correctly");

        List<String> savedToolPhotos = toolRepository.findByIdWithPhotos(savedTool.getId()).get().getPhotos();
        assertTrue(isFileExistsInMinio(savedToolPhotos.get(0)), "Minio create file correctly");
    }

    @Test
    public void create_manufacturerNotFound_returnNotFound() {
        initDataSql();
        Long manufacturerId = 999L;
        Long categoryId = categoryRepository.findAll().get(0).getId();
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Created description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "created file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "created.jpg";
            }
        });
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/tools",
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Manufacturer with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void create_categoryNotFound_returnNotFound() {
        initDataSql();
        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = 999L;
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Created description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "created file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "created.jpg";
            }
        });
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/tools",
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Category with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void create_invalidData_returnBadRequest() {
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(0L)
                .categoryId(0L)
                .type(null)
                .condition(null)
                .price(new BigDecimal("-30000"))
                .description("")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "created file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "created.jpg";
            }
        });
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/tools",
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }
    @Test
    public void create_withoutToken_returnUnauthorized() {
        initDataSql();
        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Created description")
                .build();

        byte[] fakeFile = "created file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "created.jpg";
            }
        });
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/tools",
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void update_updatesTool() {
        initDataSql();
        initDataMinio();

        Tool existingTool = toolRepository.findAll().get(0);
        Long toolId = existingTool.getId();
        List<String> existingPhotos = toolRepository.findByIdWithPhotos(toolId).get().getPhotos();

        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.SALE)
                .condition(Condition.USED)
                .price(new BigDecimal("4000"))
                .description("Updated description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of(existingPhotos.get(0)));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Tool updatedTool = toolRepository.findById(toolId).get();
        assertEquals(Type.SALE, updatedTool.getType());
        assertEquals(Condition.USED, updatedTool.getCondition());
        assertEquals(new BigDecimal("4000.00"), updatedTool.getPrice());
        assertEquals("Updated description", updatedTool.getDescription());

        List<String> updatedPhotos = toolRepository.findByIdWithPhotos(toolId).get().getPhotos();
        assertNotNull(updatedPhotos);
        assertEquals(1, updatedPhotos.size());
        assertFalse(updatedPhotos.contains(existingPhotos.get(0)), "Deleted photo should be removed");

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 1); // Wait until document appears in ES
        assertTrue(isDocumentExistsInElastic(toolId), "Elasticsearch update document correctly");

        assertTrue(isFileExistsInMinio(updatedPhotos.get(0)), "Minio create new file correctly");
        assertFalse(isFileExistsInMinio(existingPhotos.get(0)), "Deleted file should be removed from minio");
    }

    @Test
    public void update_toolNotFound_returnNotFound() {
        initDataSql();

        Tool existingTool = toolRepository.findAll().get(0);
        Long toolId = 999L;
        List<String> existingPhotos = toolRepository.findByIdWithPhotos(existingTool.getId()).get().getPhotos();

        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.SALE)
                .condition(Condition.USED)
                .price(new BigDecimal("4000"))
                .description("Updated description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of(existingPhotos.get(0)));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tool with id: %d not found".formatted(toolId), response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void update_manufacturerNotFound_returnNotFound() {
        initDataSql();

        Tool existingTool = toolRepository.findAll().get(0);
        Long toolId = existingTool.getId();
        List<String> existingPhotos = toolRepository.findByIdWithPhotos(toolId).get().getPhotos();

        Long manufacturerId = 999L;
        Long categoryId = categoryRepository.findAll().get(0).getId();

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.SALE)
                .condition(Condition.USED)
                .price(new BigDecimal("4000"))
                .description("Updated description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of(existingPhotos.get(0)));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Manufacturer with id: %d not found".formatted(manufacturerId), response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void update_categoryNotFound_returnNotFound() {
        initDataSql();

        Tool existingTool = toolRepository.findAll().get(0);
        Long toolId = existingTool.getId();
        List<String> existingPhotos = toolRepository.findByIdWithPhotos(toolId).get().getPhotos();

        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = 999L;

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.SALE)
                .condition(Condition.USED)
                .price(new BigDecimal("4000"))
                .description("Updated description")
                .build();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of(existingPhotos.get(0)));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Category with id: %d not found".formatted(categoryId), response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void update_notOwner_returnForbidden() {
        initDataSql();

        Tool existingTool = toolRepository.findAll().get(0);
        Long toolId = existingTool.getId();
        List<String> existingPhotos = toolRepository.findByIdWithPhotos(toolId).get().getPhotos();

        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.SALE)
                .condition(Condition.USED)
                .price(new BigDecimal("4000"))
                .description("Updated description")
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of(existingPhotos.get(0)));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                ResponseError.class);

        User notOwner = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: %d cannot perform actions with tool with id: %d".formatted(notOwner.getId(), toolId), response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void update_invalidData_returnBadRequest() {
        Long toolId = 0L;
        Long manufacturerId = 0L;
        Long categoryId = 0L;

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(null)
                .condition(null)
                .price(new BigDecimal("-3000"))
                .description("")
                .build();

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of("Some file"));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void update_withoutToken_returnUnauthorized() {
        initDataSql();

        Tool existingTool = toolRepository.findAll().get(0);
        Long toolId = existingTool.getId();
        List<String> existingPhotos = toolRepository.findByIdWithPhotos(toolId).get().getPhotos();

        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();

        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(manufacturerId)
                .categoryId(categoryId)
                .type(Type.SALE)
                .condition(Condition.USED)
                .price(new BigDecimal("4000"))
                .description("Updated description")
                .build();

        byte[] fakeFile = "updated file".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tool", toolCreateUpdateDto);
        body.add("files", new ByteArrayResource(fakeFile) {
            @Override
            public String getFilename() {
                return "updated.jpg";
            }
        });
        body.add("filesToDelete", List.of(existingPhotos.get(0)));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/%d".formatted(toolId),
                HttpMethod.PUT,
                request,
                ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void search_returnsFilteredResults() {
        initDataMinio();
        initDataSql();
        initDataElastic();

        Long manufacturerId = manufacturerRepository.findAll().get(0).getId();
        Long categoryId = categoryRepository.findAll().get(0).getId();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = String.format(Locale.US, "/api/v1/tools/search?manufacturer=%d&category=%d&type=%s&condition=%s&pageNumber=%d&pageSize=%d",
                manufacturerId, categoryId, "RENT", "NEW", 0, 10);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getSize());
        assertEquals(1, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill 1", result.get(0).getDescription());

        result.forEach(tool -> {
            assertNotNull(tool.getPhotos());
            tool.getPhotos().forEach(photo ->
                    assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio correctly created presigned urls")
            );
        });
    }

    @Test
    public void search_withDescriptionFilter_returnCorrectsResults() {
        initDataMinio();
        initDataSql();
        initDataElastic();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ToolDto>> response = testRestTemplate.exchange(
                "/api/v1/tools/search?description=Screw",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getSize());
        assertEquals(1, response.getBody().getTotalPages());
        List<ToolDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Screwdriver 5", result.get(0).getDescription());

        result.forEach(tool -> {
            assertNotNull(tool.getPhotos());
            tool.getPhotos().forEach(photo ->
                    assertTrue(photo.startsWith("http://10.3.34.38:80"), "Minio correctly created presigned urls")
            );
        });
    }

    @Test
    public void search_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange(
                "/api/v1/tools/search?description=Screwdri",
                HttpMethod.GET,
                null,
                ResponseError.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void delete_deletesTool() {
        initDataSql();
        initDataMinio();
        initDataElastic();
        Tool toolToDelete = toolRepository.findAll().get(0);
        Long toolToDeleteId = toolToDelete.getId();
        List<String> deleteToolPhotos = toolRepository.findByIdWithPhotos(toolToDeleteId).get().getPhotos();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = testRestTemplate.exchange(String.format("/api/v1/tools/%d", toolToDeleteId),
                HttpMethod.DELETE,
                request,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertTrue(toolRepository.findById(toolToDeleteId).isEmpty());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 4); // Wait until document deleted from ES
        assertFalse(isDocumentExistsInElastic(toolToDeleteId), "Elasticsearch delete document correctly");

        assertFalse(isFileExistsInMinio(deleteToolPhotos.get(0)), "Minio delete file correctly");
    }

    @Test
    public void delete_notOwner_returnForbidden() {
        initDataSql();
        Tool toolToDelete = toolRepository.findAll().get(0);
        Long toolToDeleteId = toolToDelete.getId();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange(String.format("/api/v1/tools/%d", toolToDeleteId),
                HttpMethod.DELETE,
                request,
                ResponseError.class);

        User notOwner = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: %d cannot perform actions with tool with id: %d".formatted(notOwner.getId(), toolToDeleteId), response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void delete_notFound_returnNotFound() {
        initDataSql();
        Long toolToDeleteId = 999L;

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange(String.format("/api/v1/tools/%d", toolToDeleteId),
                HttpMethod.DELETE,
                request,
                ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tool with id: %d not found".formatted(toolToDeleteId), response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void delete_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/tools/1",
                HttpMethod.DELETE,
                null,
                ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    private String registerAndGetToken(String login, String password) {
        Optional<User> byLogin = userRepository.findByLogin(login);
        if (byLogin.isEmpty()) {
            User user = User.builder()
                    .firstname("Ivan")
                    .lastname("Ivanov")
                    .login(login)
                    .password(passwordEncoder.encode(password))
                    .role(Role.ROLE_USER)
                    .build();

            userRepository.save(user);
        }

        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .login(login)
                .password(password)
                .build();
        ResponseEntity<AuthenticationResponse> response = testRestTemplate.postForEntity("/api/v1/auth", authenticationRequest, AuthenticationResponse.class);

        return "Bearer " + response.getBody().getToken();
    }

    private void initDataSql() {
        User owner = userRepository.save(User.builder().firstname("Ivan").lastname("Ivanov")
                .login("IvanIvanov@gmail.com").password(passwordEncoder.encode("abcde")).role(Role.ROLE_USER).build());
        Manufacturer manufacturer = manufacturerRepository.save(Manufacturer.builder().name("Makita").build());
        Category category = categoryRepository.save(Category.builder().name("Drill").build());

        List<Tool> tools = List.of(
                Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.RENT).condition(Condition.NEW)
                        .price(BigDecimal.valueOf(100)).description("Drill 1").photos(List.of(photos.get(0))).build(),
                Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.RENT).condition(Condition.USED)
                        .price(BigDecimal.valueOf(50)).description("Saw 2").photos(List.of(photos.get(1))).build(),
                Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.RENT).condition(Condition.USED)
                        .price(BigDecimal.valueOf(30)).description("Hammer 3").photos(List.of(photos.get(2))).build(),
                Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.RENT).condition(Condition.USED)
                        .price(BigDecimal.valueOf(20)).description("Wrench 4").photos(List.of(photos.get(3))).build(),
                Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.SALE).condition(Condition.NEW)
                        .price(BigDecimal.valueOf(10)).description("Screwdriver 5").photos(List.of(photos.get(4))).build()
        );

        toolRepository.saveAll(tools);
    }

    @SneakyThrows
    private void initDataMinio() {
        String bucketName = minioProperties.getBucket();

        for (String photo : photos) {
            minioClient.putObject(PutObjectArgs.builder()
                    .stream(new ByteArrayInputStream("dummy".getBytes()), "dummy".length(), -1)
                    .bucket(bucketName)
                    .object(photo)
                    .build());
        }
    }

    private void initDataElastic() {
        List<Tool> tools = toolRepository.findAll();
        List<ToolDocument> toolDocuments = tools.stream()
                .map(toolDocumentMapper::toDocument)
                .toList();

        toolDocuments.forEach(elasticService::save);

        try {
            elasticsearchClient.indices().refresh(r -> r.index(elasticProperties.getIndex()));
        } catch (IOException e) {
            throw new ElasticsearchException("Elastic search refresh failed: " + e.getMessage(), e);
        }
    }

    private boolean isDocumentExistsInElastic(Long toolID) {
        GetResponse<ToolDocument> getResponse = null;
        try {
            getResponse = elasticsearchClient.get(g -> g
                            .index(elasticProperties.getIndex())
                            .id(String.valueOf(toolID)),
                    ToolDocument.class
            );
        } catch (IOException e) {
            throw new ElasticsearchException("Get document failed: " + e.getMessage(), e);
        }
        return getResponse.found();
    }

    private boolean isFileExistsInMinio(String fileName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MinIoException("Get stat file failed: " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    private void cleanMinio() {
        String bucketName = minioProperties.getBucket();
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (bucketExists) {
            Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());

            for (Result<Item> object : objects) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(object.get().objectName())
                        .build());
            }

            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());

            minioInitializer.run();
        }
    }

    private void cleanElastic() {
        String index = elasticProperties.getIndex();

        try {
            elasticsearchClient.indices().delete(d -> d.index(index));
        } catch (IOException e) {
            throw new ElasticsearchException("Delete index failed:" + e.getMessage(), e);
        }

        try {
            elasticInitializer.run();
        } catch (Exception e) {
            throw new ElasticsearchException("Create index failed: " + e.getMessage(), e);
        }
    }

    private void cleanSql() {
        toolRepository.deleteAll();
        categoryRepository.deleteAll();
        manufacturerRepository.deleteAll();
        userRepository.deleteAll();
    }



}
