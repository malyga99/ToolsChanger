package com.example.demo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Send review request")
public class ReviewRequest {

    @NotNull(message = "Deal id must be filled in!")
    @Min(value = 1L, message = "Deal id must be greater than or equal 1!")
    @Schema(description = "Deal id to send a review", example = "1")
    private Long dealId;

    @Min(value = 1L, message = "Review rating must be greater than or equal 1!")
    @Max(value = 5L, message = "Review rating must be less than or equal 5!")
    @Schema(description = "Review rating", example = "1")
    private Integer rating;

    @NotBlank(message = "Review message must be filled in!")
    @Size(max = 255, message = "Maximum message length is 255!")
    @Schema(description = "Review message", example = "Some message")
    private String message;
}
