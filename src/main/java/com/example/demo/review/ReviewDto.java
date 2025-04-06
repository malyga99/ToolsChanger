package com.example.demo.review;

import com.example.demo.user.UserDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing reviews")
public class ReviewDto {

    @Schema(description = "Review id", example = "1")
    private Long id;

    @Schema(description = "Review sender", implementation = UserDto.class)
    private UserDto sender;

    @Schema(description = "Review recipient", implementation = UserDto.class)
    private UserDto recipient;

    @Schema(description = "Review rating", example = "1")
    private Integer rating;

    @Schema(description = "Review message", example = "Some message")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Review creation date", example = "2023-01-01 12:00")
    private LocalDateTime createdAt;
}
