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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tool purchase request")
public class PurchaseRequest {

    @NotNull(message = "Tool id must be filled in!")
    @Min(value = 1L, message = "Tool id must be greater than 1!")
    @Schema(description = "Tool id for purchase", example = "1")
    private Long toolId;

    @NotNull(message = "Price must be filled in!")
    @Positive(message = "Price must be greater than 0!")
    @Schema(description = "Price per purchase", example = "3000")
    private BigDecimal price;

    @Size(max = 255, message = "Maximum message length is 255")
    @Schema(description = "Message about purchase", example = "Some message")
    private String message;
}
