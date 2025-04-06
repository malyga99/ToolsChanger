package com.example.demo.manufacturer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a manufacturer's details")
public class ManufacturerDto {

    @Schema(description = "Manufacturer id", example = "1")
    private Long id;

    @Schema(description = "Manufacturer name", example = "Makita")
    private String name;
}
