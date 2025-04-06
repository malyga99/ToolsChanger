package com.example.demo.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolDocument {

    private Long id;

    private Long manufacturer;

    private Long category;

    private String type;

    private String condition;

    private BigDecimal price;

    private String description;

}
