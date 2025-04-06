package com.example.demo.review;

import com.example.demo.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final UserMapper userMapper;

    public ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .sender(userMapper.toDto(review.getSender()))
                .recipient(userMapper.toDto(review.getRecipient()))
                .rating(review.getRating())
                .message(review.getMessage())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
