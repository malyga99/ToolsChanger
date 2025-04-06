package com.example.demo.review;

import com.example.demo.deal.DealDto;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ReviewMapper reviewMapper;

    @Test
    public void toDto_returnCorrectlyDto() {
        User sender = User.builder().lastname("Ivanov").build();
        User recipient = User.builder().lastname("Ivanov2").build();
        Review review = Review.builder()
                .id(1L)
                .sender(sender)
                .recipient(recipient)
                .rating(5)
                .message("Some message")
                .createdAt(LocalDateTime.now())
                .build();

        when(userMapper.toDto(sender)).thenReturn(UserDto.builder().lastname("Ivanov").build());
        when(userMapper.toDto(recipient)).thenReturn(UserDto.builder().lastname("Ivanov2").build());

        ReviewDto result = reviewMapper.toDto(review);

        assertNotNull(result);
        assertEquals(review.getId(), result.getId());
        assertEquals(sender.getLastname(), result.getSender().getLastname());
        assertEquals(recipient.getLastname(), result.getRecipient().getLastname());
        assertEquals(review.getRating(), result.getRating());
        assertEquals(review.getMessage(), result.getMessage());
        assertNotNull(result.getCreatedAt());
    }
}