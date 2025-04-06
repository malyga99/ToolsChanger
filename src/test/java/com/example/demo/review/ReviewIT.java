package com.example.demo.review;

import com.example.demo.BaseIT;
import com.example.demo.PaginatedResponse;
import com.example.demo.auth.AuthenticationRequest;
import com.example.demo.auth.AuthenticationResponse;
import com.example.demo.category.Category;
import com.example.demo.category.CategoryRepository;
import com.example.demo.deal.Deal;
import com.example.demo.deal.DealRepository;
import com.example.demo.deal.Status;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewIT extends BaseIT {

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
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        cleanSql();
    }

    @Test
    public void send_saveReviewCorrectly() {
        initDataSql();
        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(dealId)
                .rating(5)
                .message("Some message")
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ReviewDto> response = testRestTemplate.postForEntity("/api/v1/reviews", request, ReviewDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        User sender = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        User recipient = userRepository.findByLogin("IvanIvanov@gmail.com").get();
        ReviewDto savedReview = response.getBody();
        assertNotNull(savedReview);
        assertEquals(sender.getFirstname(), savedReview.getSender().getFirstname());
        assertEquals(recipient.getFirstname(), savedReview.getRecipient().getFirstname());
        assertEquals(reviewRequest.getRating(), savedReview.getRating());
        assertEquals(reviewRequest.getMessage(), savedReview.getMessage());
        assertNotNull(savedReview.getCreatedAt());

        List<Review> reviews = reviewRepository.findAll();
        assertEquals(1, reviews.size());
        assertEquals(reviews.get(0).getRating(), reviewRequest.getRating());
    }

    @Test
    public void send_invalidData_returnBadRequest() {
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(-1L)
                .rating(-5)
                .message(null)
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/reviews", request, ResponseError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void send_dealNotFound_returnNotFound() {
        initDataSql();
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(999L)
                .rating(5)
                .message("Some message")
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/reviews", request, ResponseError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Deal with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void send_nonApprovedDeal_returnForbidden() {
        initDataSql();
        Deal fromDb = dealRepository.findAll().get(0);
        fromDb.setStatus(Status.PENDING);
        Deal nonApprovedDeal = dealRepository.save(fromDb);
        Long nonApprovedDealId = nonApprovedDeal.getId();

        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(nonApprovedDealId)
                .rating(5)
                .message("Some message")
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/reviews", request, ResponseError.class);

        User requester = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: %d cannot send review for a non-approved deal with id: %d".formatted(requester.getId(), nonApprovedDealId),
                response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void send_userNotParticipantInDeal_returnForbidden() {
        initDataSql();
        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(dealId)
                .rating(5)
                .message("Some message")
                .build();

        String token = registerAndGetToken("NotParticipant@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/reviews", request, ResponseError.class);

        User requester = userRepository.findByLogin("NotParticipant@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: %d does not participate in the deal with id: %d".formatted(requester.getId(), dealId),
                response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void send_userAlreadySentReview_returnForbidden() {
        initDataSql();
        Deal deal = dealRepository.findAll().get(0);
        Long dealId = deal.getId();
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(dealId)
                .rating(5)
                .message("Some message")
                .build();

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ReviewDto> firstResponse = testRestTemplate.postForEntity("/api/v1/reviews", request, ReviewDto.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        ResponseEntity<ResponseError> secondResponse = testRestTemplate.postForEntity("/api/v1/reviews", request, ResponseError.class);

        User requester = userRepository.findByLogin("IvanIvanov2@gmail.com").get();
        assertEquals(HttpStatus.FORBIDDEN, secondResponse.getStatusCode());
        assertNotNull(secondResponse.getBody());
        assertEquals("User with id: %d has already sent a review on the deal with id: %d".formatted(requester.getId(), dealId),
                secondResponse.getBody().getMessage());
        assertEquals(403, secondResponse.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(secondResponse.getBody().getTime()));
    }

    @Test
    public void send_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.postForEntity("/api/v1/reviews", null, ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void send_deleteFromCacheCorrectly() {
        initDataSql();
        sendOneReview();
        Cache cache = cacheManager.getCache("review");
        assertNotNull(cache);

        Long userId = userRepository.findByLogin("IvanIvanov@gmail.com").get().getId();
        assertNull(cache.get(userId));

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Double> avgResponse = testRestTemplate.exchange("/api/v1/reviews/" + userId + "/rating",
                HttpMethod.GET,
                request,
                Double.class
        );
        assertNotNull(avgResponse.getBody());

        Cache.ValueWrapper averageRating = cache.get(userId);
        assertNotNull(averageRating);
        assertNotNull(averageRating.get());

        Long dealId = dealRepository.findAll().get(1).getId();
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(dealId)
                .rating(5)
                .message("Some message")
                .build();
        HttpEntity<ReviewRequest> reviewRequestBody = new HttpEntity<>(reviewRequest, headers);

        ResponseEntity<ReviewDto> response = testRestTemplate.postForEntity("/api/v1/reviews", reviewRequestBody, ReviewDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        assertNull(cache.get(userId));
    }

    @Test
    public void findByUser_returnAllReviews() {
        initDataSql();
        sendReviews();
        Long userId = userRepository.findByLogin("IvanIvanov@gmail.com").get().getId();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ReviewDto>> response = testRestTemplate.exchange("/api/v1/reviews/" + userId + "?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalElements());
        assertEquals(5, response.getBody().getSize());
        assertEquals(1, response.getBody().getTotalPages());
        List<ReviewDto> result = response.getBody().getContent();
        assertNotNull(result);

        for (ReviewDto reviewDto : result) {
            assertEquals(5, reviewDto.getRating());
        }
    }

    @Test
    public void findByUser_withNoReviews_returnEmptyList() {
        initDataSql();
        Long userId = userRepository.findByLogin("IvanIvanov@gmail.com").get().getId();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaginatedResponse<ReviewDto>> response = testRestTemplate.exchange("/api/v1/reviews/" + userId + "?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        List<ReviewDto> result = response.getBody().getContent();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void findByUser_userNotFound_returnNotFound() {
        initDataSql();
        long userId = 999L;

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/reviews/" + userId + "?pageNumber=0&pageSize=5",
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void findByUser_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.getForEntity("/api/v1/reviews/1?pageNumber=0&pageSize=10", ResponseError.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full authentication is required to access this resource", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void getAverageRatingByUser_returnCorrectlyAverageRating() {
        initDataSql();
        sendReviews();

        Long userId = userRepository.findByLogin("IvanIvanov@gmail.com").get().getId();

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Double> response = testRestTemplate.exchange("/api/v1/reviews/" + userId + "/rating",
                HttpMethod.GET,
                request,
                Double.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Double rating = response.getBody();
        assertEquals(5.0, rating);
    }

    @Test
    public void getAverageRatingByUser_userNotFound_returnNotFound() {
        initDataSql();

        long userId = 999L;

        String token = registerAndGetToken("IvanIvanov@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/reviews/" + userId + "/rating",
                HttpMethod.GET,
                request,
                ResponseError.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with id: 999 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(LocalDateTime.now().isAfter(response.getBody().getTime()));
    }

    @Test
    public void getAverageRatingByUser_withoutToken_returnUnauthorized() {
        ResponseEntity<ResponseError> response = testRestTemplate.exchange("/api/v1/reviews/1/rating",
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
    public void getAverageRatingByUser_cacheAverageRatingCorrectly() {
        initDataSql();
        sendReviews();
        Cache cache = cacheManager.getCache("review");
        assertNotNull(cache);

        Long userId = userRepository.findByLogin("IvanIvanov@gmail.com").get().getId();
        assertNull(cache.get(userId));

        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        long start = System.currentTimeMillis();
        ResponseEntity<Double> firstResponse = testRestTemplate.exchange("/api/v1/reviews/" + userId + "/rating",
                HttpMethod.GET,
                request,
                Double.class
        );
        long firstDuration = System.currentTimeMillis() - start;
        assertNotNull(firstResponse.getBody());

        Cache.ValueWrapper averageRating = cache.get(userId);
        assertNotNull(averageRating);
        assertNotNull(averageRating.get());
        assertEquals(5.0, averageRating.get());

        start = System.currentTimeMillis();
        ResponseEntity<Double> secondResponse = testRestTemplate.exchange("/api/v1/reviews/" + userId + "/rating",
                HttpMethod.GET,
                request,
                Double.class
        );
        long secondDuration = System.currentTimeMillis() - start;

        assertNotNull(secondResponse.getBody());
        assertEquals(firstResponse.getBody(), secondResponse.getBody());
        assertTrue(secondDuration < firstDuration / 2, "Get from cache");

    }

    private void sendReviews() {
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(0L)
                .rating(5)
                .message("Some message")
                .build();

        List<Deal> deals = dealRepository.findAll();
        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        for (Deal deal : deals) {
            reviewRequest.setDealId(deal.getId());
            testRestTemplate.postForEntity("/api/v1/reviews", request, ReviewDto.class);
        }
    }

    private void sendOneReview() {
        Long dealId = dealRepository.findAll().get(0).getId();
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .dealId(dealId)
                .rating(5)
                .message("Some message")
                .build();
        String token = registerAndGetToken("IvanIvanov2@gmail.com", "abcde");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<ReviewRequest> request = new HttpEntity<>(reviewRequest, headers);

        testRestTemplate.postForEntity("/api/v1/reviews", request, ReviewDto.class);
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
        User requester = userRepository.save(User.builder().firstname("Ivan").lastname("Ivanov")
                .login("IvanIvanov2@gmail.com").password(passwordEncoder.encode("abcde")).role(Role.ROLE_USER).build());
        Manufacturer manufacturer = manufacturerRepository.save(Manufacturer.builder().name("Makita").build());
        Category category = categoryRepository.save(Category.builder().name("Drill").build());
        Tool tool = toolRepository.save(Tool.builder().owner(owner).manufacturer(manufacturer).category(category).type(Type.RENT).condition(Condition.NEW)
                .price(BigDecimal.valueOf(100)).description("Drill 1").photos(List.of("photo1.jpg")).build());

        LocalDateTime now = LocalDateTime.now();
        List<Deal> deals = List.of(
                Deal.builder().owner(owner).requester(requester).tool(tool).price(BigDecimal.valueOf(300)).message("Some message")
                        .status(Status.APPROVED).startDate(now).endDate(now).build(),
                Deal.builder().owner(owner).requester(requester).tool(tool).price(BigDecimal.valueOf(300)).message("Some message")
                        .status(Status.APPROVED).startDate(now).endDate(now).build(),
                Deal.builder().owner(owner).requester(requester).tool(tool).price(BigDecimal.valueOf(300)).message("Some message")
                        .status(Status.APPROVED).startDate(now).endDate(now).build()
        );

        dealRepository.saveAll(deals);
    }

    private void cleanSql() {
        reviewRepository.deleteAll();
        dealRepository.deleteAll();
        toolRepository.deleteAll();
        categoryRepository.deleteAll();
        manufacturerRepository.deleteAll();
        userRepository.deleteAll();
    }

}
