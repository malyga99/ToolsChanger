package com.example.demo.tool;

import com.example.demo.category.CategoryDto;
import com.example.demo.manufacturer.ManufacturerDto;
import com.example.demo.user.UserDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a tool's details")
public class ToolDto {

    @Schema(description = "Tool id", example = "1")
    private Long id;

    @Schema(description = "Owner of the tool", implementation = UserDto.class)
    private UserDto owner;

    @Schema(description = "Manufacturer details", implementation = ManufacturerDto.class)
    private ManufacturerDto manufacturer;

    @Schema(description = "Category details", implementation = CategoryDto.class)
    private CategoryDto category;

    @Schema(description = "Type of the tool", example = "RENT")
    private Type type;

    @Schema(description = "Condition of the tool", example = "NEW")
    private Condition condition;

    @Schema(description = "Price per tool", example = "3000")
    private BigDecimal price;

    @Schema(description = "Tool description", example = "High-quality power drill")
    private String description;

    @Schema(description = "List of photo URLs", example = "[\"url1\", \"url2\"]")
    private List<String> photos;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Tool creation date", example = "2023-01-01 12:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Tool update date", example = "2023-01-01 12:00")
    private LocalDateTime updatedAt;
}
