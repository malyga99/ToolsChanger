package com.example.demo.deal;

import com.example.demo.BaseIT;
import com.example.demo.PaginatedResponse;
import com.example.demo.auth.AuthenticationRequest;
import com.example.demo.auth.AuthenticationResponse;
import com.example.demo.category.Category;
import com.example.demo.category.CategoryRepository;
import com.example.demo.exception.ResponseError;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerRepository;
import com.example.demo.tool.Condition;
import com.example.demo.tool.Tool;
import com.example.demo.tool.ToolRepository;
import com.example.demo.tool.Type;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DealIT extends BaseIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private final List<String> photos = List.of("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg", "photo5.jpg");

    @BeforeEach
    public void setup() {
        cleanSql();
    }

    @Test
    public void rent_saveDealRequestCorrectly() {
        initDataSql();
        Tool tool = toolRepository.findAll().get(0);
        Long toolId = tool.getId();
        RentalRequest rentalRequest = RentalRequest.builder()
                .toolId(toolId)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<RentalRequest> request = new HttpEntity<>(rentalRequest, headers);

        ResponseEntity<DealDto> response = testRestTemplate.postForEntity("/api/v1/deals/rental", request, DealDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        User renter = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        DealDto savedDeal = response.getBody();
        assertNotNull(savedDeal);
        assertEquals(tool.getOwner().getFirstname(), savedDeal.getOwner().getFirstname());
        assertEquals(renter.getFirstname(), savedDeal.getRequester().getFirstname());
        assertEquals(rentalRequest.getToolId(), savedDeal.getTool().getId());
        assertEquals(rentalRequest.getMessage(), savedDeal.getMessage());
        assertEquals(rentalRequest.getPrice(), savedDeal.getPrice());
        assertNotNull(rentalRequest.getStartDate());
        assertNotNull(rentalRequest.getEndDate());
        assertEquals(Status.PENDING, savedDeal.getStatus());

        List<Deal> deals = dealRepository.findAll();
        assertEquals(1, deals.size());
        assertEquals(rentalRequest.getMessage(), deals.get(0).getMessage());
    }

    @Test
    public void rent_invalidData_returnBadRequest() {
        RentalRequest rentalRequest = RentalRequest.builder()
                .toolId(-1L)
                .message(null)
                .startDate(null)
                .endDate(null)
                .price(BigDecimal.valueOf(-3000L))
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<RentalRequest> request = new HttpEntity<>(rentalRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/rental", request, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void rent_toolNotFound_returnNotFound() {
        initDataSql();
        RentalRequest rentalRequest = RentalRequest.builder()
                .toolId(999L)
                .message("Some message")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .price(BigDecimal.valueOf(3000L))
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<RentalRequest> request = new HttpEntity<>(rentalRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/rental", request, ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tool with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void rent_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/rental", null, ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void purchase_saveDealRequestCorrectly() {
        initDataSql();
        Tool tool = toolRepository.findAll().get(0);
        Long toolId = tool.getId();
        PurchaseRequest purchaseRequest = PurchaseRequest.builder()
                .toolId(toolId)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<PurchaseRequest> request = new HttpEntity<>(purchaseRequest, headers);

        ResponseEntity<DealDto> response = testRestTemplate.postForEntity("/api/v1/deals/purchase", request, DealDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        User buyer = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        DealDto savedDeal = response.getBody();
        assertNotNull(savedDeal);
        assertEquals(tool.getOwner().getFirstname(), savedDeal.getOwner().getFirstname());
        assertEquals(buyer.getFirstname(), savedDeal.getRequester().getFirstname());
        assertEquals(purchaseRequest.getToolId(), savedDeal.getTool().getId());
        assertEquals(purchaseRequest.getMessage(), savedDeal.getMessage());
        assertEquals(purchaseRequest.getPrice(), savedDeal.getPrice());
        assertNull(savedDeal.getStartDate());
        assertNull(savedDeal.getEndDate());
        assertEquals(Status.PENDING, savedDeal.getStatus());

        List<Deal> deals = dealRepository.findAll();
        assertEquals(1, deals.size());
        assertEquals(purchaseRequest.getMessage(), deals.get(0).getMessage());
    }

    @Test
    public void purchase_invalidData_returnBadRequest() {
        PurchaseRequest purchaseRequest = PurchaseRequest.builder()
                .toolId(-1L)
                .message(null)
                .price(BigDecimal.valueOf(-3000L))
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<PurchaseRequest> request = new HttpEntity<>(purchaseRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/purchase", request, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void purchase_toolNotFound_returnNotFound() {
        initDataSql();
        PurchaseRequest purchaseRequest = PurchaseRequest.builder()
                .toolId(999L)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<PurchaseRequest> request = new HttpEntity<>(purchaseRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/purchase", request, ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tool with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void purchase_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/purchase", null, ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void findRequestsSentToMe_returnAllDealRequests() {
        initDataSql();
        sendDealRequests();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<DealDto>> response = testRestTemplate.exchange("/api/v1/deals?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalElements());
        assertEquals(5, response.getBody().getSize());
        assertEquals(1, response.getBody().getTotalPages());
        List<DealDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Drill 1", result.get(0).getTool().getDescription());
        assertEquals("Saw 2", result.get(1).getTool().getDescription());
        assertEquals("Hammer 3", result.get(2).getTool().getDescription());
    }

    @Test
    public void findRequestsSentToMe_withNoDealRequests_returnEmptyList() {
        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<DealDto>> response = testRestTemplate.exchange("/api/v1/deals?pageNumber=0&pageSize=10",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        List<DealDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void findRequestsSentToMe_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/deals?pageNumber=0&pageSize=10",
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
    public void findRequestsSentToMeByStatus_returnAllDealRequests() {
        initDataSql();
        sendDealRequests();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<DealDto>> response = testRestTemplate.exchange("/api/v1/deals/status?status=PENDING&pageNumber=0&pageSize=10",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalElements());
        assertEquals(10, response.getBody().getSize());
        assertEquals(1, response.getBody().getTotalPages());
        List<DealDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Drill 1", result.get(0).getTool().getDescription());
        assertEquals("Saw 2", result.get(1).getTool().getDescription());
        assertEquals("Hammer 3", result.get(2).getTool().getDescription());

        for (DealDto dealDto : result) {
            assertEquals(Status.PENDING, dealDto.getStatus());
        }
    }

    @Test
    public void findRequestsSentToMeByStatus_withNoDealRequestsByStatus_returnEmptyList() {
        initDataSql();
        sendDealRequests();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<DealDto>> response = testRestTemplate.exchange("/api/v1/deals/status?status=APPROVED&pageNumber=0&pageSize=10",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        List<DealDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void findRequestsSentToMeByStatus_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/deals?status=PENDING&pageNumber=0&pageSize=10",
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
    public void confirm_confirmDealRequestSuccessfully() {
        initDataSql();
        sendDealRequests();

        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();
        assertEquals(Status.PENDING, deal.getStatus());

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = testRestTemplate.postForEntity("/api/v1/deals/" + dealId + "/confirm", request, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Deal confirmedDeal = dealRepository.findById(dealId).get();
        assertEquals(Status.APPROVED, confirmedDeal.getStatus());
    }

    @Test
    public void confirm_dealRequestNotFound_returnNotFound() {
        initDataSql();
        sendDealRequests();

        long dealId = 999L;

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/" + dealId + "/confirm", request, ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Deal with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void confirm_userDoesNotHavePermission_returnForbidden() {
        initDataSql();
        sendDealRequests();

        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/" + dealId + "/confirm", request, ResponseError.class);

        User notOwner = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: %d cannot modify deal with id: %d".formatted(notOwner.getId(), deal.getId()), response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void cancel_cancelDealRequestSuccessfully() {
        initDataSql();
        sendDealRequests();

        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();
        assertEquals(Status.PENDING, deal.getStatus());

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = testRestTemplate.postForEntity("/api/v1/deals/" + dealId + "/cancel", request, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Deal confirmedDeal = dealRepository.findById(dealId).get();
        assertEquals(Status.REJECTED, confirmedDeal.getStatus());
    }

    @Test
    public void cancel_dealRequestNotFound_returnNotFound() {
        initDataSql();
        sendDealRequests();

        long dealId = 999L;

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/" + dealId + "/cancel", request, ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Deal with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void cancel_userDoesNotHavePermission_returnForbidden() {
        initDataSql();
        sendDealRequests();

        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/deals/" + dealId + "/cancel", request, ResponseError.class);

        User notOwner = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: %d cannot modify deal with id: %d".formatted(notOwner.getId(), deal.getId()), response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    private void sendDealRequests() {
        List<Tool> tools = toolRepository.findAll();
        RentalRequest rentalRequest = RentalRequest.builder()
                .toolId(0L)
                .message("Some message")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .price(BigDecimal.valueOf(3000L))
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        for (Tool tool : tools) {
            rentalRequest.setToolId(tool.getId());
            HttpEntity<RentalRequest> request = new HttpEntity<>(rentalRequest, headers);
            testRestTemplate.postForEntity("/api/v1/deals/rental", request, DealDto.class);
        }
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
                Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.RENT).condition(Condition.NEW)
                        .price(BigDecimal.valueOf(30)).description("Hammer 3").photos(List.of(photos.get(2))).build());

        toolRepository.saveAll(tools);
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

    private void cleanSql() {
        dealRepository.deleteAll();
        toolRepository.deleteAll();
        categoryRepository.deleteAll();
        manufacturerRepository.deleteAll();
        userRepository.deleteAll();
    }
}
