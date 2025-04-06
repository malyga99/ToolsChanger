package com.example.demo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a categories details")
public class CategoryDto {

    @Schema(description = "Category id", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Hammer")
    private String name;
}
