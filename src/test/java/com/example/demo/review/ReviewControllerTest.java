package com.example.demo.review;

import com.example.demo.exception.DealNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.handler.GlobalHandler;
import com.example.demo.user.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    private ReviewRequest reviewRequest;

    private ReviewDto firstReviewDto;

    private ReviewDto secondReviewDto;

    private Pageable mockPageable;

    private Page<ReviewDto> mockPage;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalHandler())
                .build();
        reviewRequest = ReviewRequest.builder()
                .dealId(1L)
                .rating(5)
                .message("Some message")
                .build();
        firstReviewDto = ReviewDto.builder()
                .id(1L)
                .sender(UserDto.builder().id(1L).build())
                .recipient(UserDto.builder().id(2L).build())
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();
        secondReviewDto = ReviewDto.builder()
                .id(2L)
                .sender(UserDto.builder().id(1L).build())
                .recipient(UserDto.builder().id(2L).build())
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();
        mockPageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt");
        mockPage = new PageImpl<>(List.of(firstReviewDto, secondReviewDto), mockPageable, 2);
    }

    @Test
    public void send_sendReviewCorrectly() throws Exception {
        String reviewRequestJson = objectMapper.writeValueAsString(reviewRequest);
        when(reviewService.send(reviewRequest)).thenReturn(firstReviewDto);

        mockMvc.perform(post("/api/v1/reviews")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewRequestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/reviews/" + firstReviewDto.getId()))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sender.id").value(1))
                .andExpect(jsonPath("$.recipient.id").value(2))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.message").value("Some message"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(reviewService, times(1)).send(reviewRequest);
    }

    @Test
    public void send_invalidData_returnBadRequest() throws Exception {
        ReviewRequest invalidRequest = ReviewRequest.builder()
                .dealId(-1L)
                .rating(-5)
                .message(null)
                .build();
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/reviews")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.time").exists());
    }

    @Test
    public void send_dealNotFound_returnBadRequest() throws Exception {
        String reviewRequestJson = objectMapper.writeValueAsString(reviewRequest);
        when(reviewService.send(reviewRequest)).thenThrow(new DealNotFoundException("Deal with id: 1 not found"));

        mockMvc.perform(post("/api/v1/reviews")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Deal with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(reviewService, times(1)).send(reviewRequest);
    }

    @Test
    public void send_dontHavePermission_returnForbidden() throws Exception {
        String reviewRequestJson = objectMapper.writeValueAsString(reviewRequest);
        when(reviewService.send(reviewRequest)).thenThrow(new UserDontHavePermissionException("User with id: 1 has already sent a review on the deal with id: 1"));

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(reviewRequestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User with id: 1 has already sent a review on the deal with id: 1"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.time").exists());

        verify(reviewService, times(1)).send(reviewRequest);
    }

    @Test
    public void findByUser_returnTwoReviews() throws Exception {
        when(reviewService.findByUser(1L, mockPageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/reviews/{id}", 1L)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].sender.id").value(1))
                .andExpect(jsonPath("$.content[0].recipient.id").value(2))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[0].message").value("Some message"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].sender.id").value(1))
                .andExpect(jsonPath("$.content[1].recipient.id").value(2))
                .andExpect(jsonPath("$.content[1].rating").value(5))
                .andExpect(jsonPath("$.content[1].message").value("Some message"));

        verify(reviewService, times(1)).findByUser(1L, mockPageable);
    }

    @Test
    public void findByUser_userNotFound_returnNotFound() throws Exception {
        when(reviewService.findByUser(1L, mockPageable)).thenThrow(new UserNotFoundException("User with id: 1 not found"));

        mockMvc.perform(get("/api/v1/reviews/{id}", 1L)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(reviewService, times(1)).findByUser(1L, mockPageable);
    }

    @Test
    public void getAverageRatingByUser_returnAverageRating() throws Exception {
        when(reviewService.getAverageRatingByUser(1L)).thenReturn(4.9);

        mockMvc.perform(get("/api/v1/reviews/{id}/rating", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.9));

        verify(reviewService, times(1)).getAverageRatingByUser(1L);
    }

    @Test
    public void getAverageRatingByUser_userNotFound_returnNotFound() throws Exception {
        when(reviewService.getAverageRatingByUser(1L)).thenThrow(new UserNotFoundException("User with id: 1 not found"));

        mockMvc.perform(get("/api/v1/reviews/{id}/rating", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(reviewService, times(1)).getAverageRatingByUser(1L);
    }
}