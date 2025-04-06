package com.example.demo.elasticsearch;

import com.example.demo.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class ToolDocumentMapper {

    public ToolDocument toDocument(Tool tool) {
        return ToolDocument.builder()
                .id(tool.getId())
                .manufacturer(tool.getManufacturer().getId())
                .category(tool.getCategory().getId())
                .type(tool.getType().name())
                .condition(tool.getCondition().name())
                .price(tool.getPrice())
                .description(tool.getDescription())
                .build();
    }
}
