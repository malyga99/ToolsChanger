package com.example.demo.review;

import com.example.demo.deal.Deal;
import com.example.demo.deal.DealDto;
import com.example.demo.deal.DealRepository;
import com.example.demo.deal.Status;
import com.example.demo.exception.DealNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.tool.Tool;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewRequest reviewRequest;

    private ReviewDto firstReviewDto;

    private ReviewDto secondReviewDto;

    private Review firstReview;

    private Review secondReview;

    private User owner;

    private User requester;

    private Deal deal;

    private Pageable mockPageable;

    private Page<Review> mockPage;

    @BeforeEach
    public void setup() {
        owner = User.builder().id(1L).login("IvanIvanov@gmail.com").build();
        requester = User.builder().id(2L).login("IvanIvanov2@gmail.com").build();
        deal = Deal.builder()
                .id(1L)
                .owner(owner)
                .requester(requester)
                .tool(Tool.builder().id(1L).owner(owner).build())
                .price(BigDecimal.valueOf(3000L))
                .message("Some message")
                .status(Status.APPROVED)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .build();
        firstReview = Review.builder()
                .id(1L)
                .sender(requester)
                .recipient(owner)
                .deal(deal)
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();
        secondReview = Review.builder()
                .id(1L)
                .sender(owner)
                .recipient(requester)
                .deal(deal)
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();
        firstReviewDto = ReviewDto.builder()
                .id(1L)
                .sender(UserDto.builder().id(2L).build())
                .recipient(UserDto.builder().id(1L).build())
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();
        secondReviewDto = ReviewDto.builder()
                .id(1L)
                .sender(UserDto.builder().id(1L).build())
                .recipient(UserDto.builder().id(2L).build())
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();
        reviewRequest = ReviewRequest.builder()
                .dealId(1L)
                .rating(5)
                .message("Some message")
                .build();
        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstReview, secondReview), mockPageable, 2);
    }

    @Test
    public void send_requesterSendReview_returnSavedReview() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(reviewRepository.existsBySenderAndDeal(requester, deal)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(firstReview);
        when(reviewMapper.toDto(firstReview)).thenReturn(firstReviewDto);
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);

        ReviewDto result = reviewService.send(reviewRequest);

        assertNotNull(result);
        assertEquals(firstReviewDto, result);

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).existsBySenderAndDeal(requester, deal);
        verify(reviewMapper, times(1)).toDto(firstReview);
        verify(reviewRepository, times(1)).save(argumentCaptor.capture());

        Review review = argumentCaptor.getValue();
        assertNotNull(review);
        assertEquals(requester, review.getSender());
        assertEquals(owner, review.getRecipient());
        assertEquals(deal, review.getDeal());
        assertEquals(reviewRequest.getRating(), review.getRating());
        assertEquals(reviewRequest.getMessage(), review.getMessage());
    }

    @Test
    public void send_ownerSendReview_returnSavedReview() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(reviewRepository.existsBySenderAndDeal(owner, deal)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(secondReview);
        when(reviewMapper.toDto(secondReview)).thenReturn(secondReviewDto);
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);

        ReviewDto result = reviewService.send(reviewRequest);

        assertNotNull(result);
        assertEquals(secondReviewDto, result);

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).existsBySenderAndDeal(owner, deal);
        verify(reviewMapper, times(1)).toDto(secondReview);
        verify(reviewRepository, times(1)).save(argumentCaptor.capture());

        Review review = argumentCaptor.getValue();
        assertNotNull(review);
        assertEquals(owner, review.getSender());
        assertEquals(requester, review.getRecipient());
        assertEquals(deal, review.getDeal());
        assertEquals(reviewRequest.getRating(), review.getRating());
        assertEquals(reviewRequest.getMessage(), review.getMessage());
    }

    @Test
    public void send_dealNotFound_returnNotFound() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(dealRepository.findById(1L)).thenReturn(Optional.empty());

        DealNotFoundException dealNotFoundException = assertThrows(DealNotFoundException.class, () -> reviewService.send(reviewRequest));
        assertEquals("Deal with id: 1 not found", dealNotFoundException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    public void send_nonApprovedDeal_returnForbidden() {
        deal.setStatus(Status.PENDING);
        when(userService.getCurrentUser()).thenReturn(requester);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        UserDontHavePermissionException userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> reviewService.send(reviewRequest));
        assertEquals("User with id: %d cannot send review for a non-approved deal with id: %d".formatted(requester.getId(), deal.getId()),
                userDontHavePermissionException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    public void send_userNotParticipantInDeal_returnForbidden() {
        User user = User.builder().id(999L).login("AntonAntonov@gmail.com").build();
        when(userService.getCurrentUser()).thenReturn(user);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));

        UserDontHavePermissionException userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> reviewService.send(reviewRequest));
        assertEquals("User with id: %d does not participate in the deal with id: %d".formatted(user.getId(), deal.getId()),
                userDontHavePermissionException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    public void send_userAlreadySentReview_returnForbidden() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(reviewRepository.existsBySenderAndDeal(requester, deal)).thenReturn(true);

        UserDontHavePermissionException userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> reviewService.send(reviewRequest));
        assertEquals("User with id: %d has already sent a review on the deal with id: %d".formatted(requester.getId(), deal.getId()),
                userDontHavePermissionException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).existsBySenderAndDeal(requester, deal);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void findByUser_returnTwoReviews() {
        when(userService.findById(1L)).thenReturn(owner);
        when(reviewRepository.findByRecipient(mockPageable, owner)).thenReturn(mockPage);
        when(reviewMapper.toDto(firstReview)).thenReturn(firstReviewDto);
        when(reviewMapper.toDto(secondReview)).thenReturn(secondReviewDto);

        Page<ReviewDto> result = reviewService.findByUser(1L, mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstReviewDto, result.getContent().get(0));
        assertEquals(secondReviewDto, result.getContent().get(1));

        verify(userService, times(1)).findById(1L);
        verify(reviewRepository, times(1)).findByRecipient(mockPageable, owner);
        verify(reviewMapper, times(1)).toDto(firstReview);
        verify(reviewMapper, times(1)).toDto(secondReview);
    }

    @Test
    public void getAverageRatingByUser_returnAverageRating() {
        when(userService.findById(1L)).thenReturn(owner);
        when(reviewRepository.findAverageRatingByRecipient(owner.getId())).thenReturn(4.9);

        Double result = reviewService.getAverageRatingByUser(1L);

        assertNotNull(result);
        assertEquals(4.9, result);

        verify(userService, times(1)).findById(1L);
        verify(reviewRepository, times(1)).findAverageRatingByRecipient(owner.getId());
    }

}