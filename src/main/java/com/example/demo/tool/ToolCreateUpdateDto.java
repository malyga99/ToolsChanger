package com.example.demo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating a tool")
public class ToolCreateUpdateDto {

    @NotNull(message = "Manufacturer id must be filled in!")
    @Min(value = 1L, message = "Manufacturer id must be greater than or equal 1!")
    @Schema(description = "Manufacturer id", example = "1")
    private Long manufacturerId;

    @NotNull(message = "Category id must be filled in!")
    @Min(value = 1L, message = "Category id must be greater than or equal 1!")
    @Schema(description = "Category id", example = "1")
    private Long categoryId;

    @NotNull(message = "Type must be filled in!")
    @Schema(description = "Type of the tool", example = "RENT")
    private Type type;

    @NotNull(message = "Condition must be filled in!")
    @Schema(description = "Condition of the tool", example = "NEW")
    private Condition condition;

    @NotNull(message = "Price must be filled in!")
    @Positive(message = "Price must be greater than or equal 0!")
    @Schema(description = "Price per tool", example = "3000")
    private BigDecimal price;

    @NotBlank(message = "Description must be filled in!")
    @Size(max = 1000, message = "Maximum description length is 1000 characters!")
    @Schema(description = "Tool description", example = "High-quality power drill")
    private String description;

}
