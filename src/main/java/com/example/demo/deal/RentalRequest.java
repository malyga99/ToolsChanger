package com.example.demo.deal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tool rental request")
public class RentalRequest {

    @NotNull(message = "Tool id must be filled in!")
    @Min(value = 1L, message = "Tool id must be greater than or equal 1!")
    @Schema(description = "Tool id for rental", example = "1")
    private Long toolId;

    @NotNull(message = "Price must be filled in!")
    @Positive(message = "Price id must be greater than or equal 0!")
    @Schema(description = "Price per rental", example = "3000")
    private BigDecimal price;

    @Size(max = 255, message = "Maximum message length is 255")
    @Schema(description = "Message about rental", example = "Some message")
    private String message;

    @NotNull(message = "Start date must be filled in!")
    @Schema(description = "Start of rental", example = "2023-01-01 12:00")
    private LocalDateTime startDate;

    @NotNull(message = "End date must be filled in!")
    @Schema(description = "End of rental", example = "2023-01-01 12:00")
    private LocalDateTime endDate;
}
