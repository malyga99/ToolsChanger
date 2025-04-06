package com.example.demo.elasticsearch;

import com.example.demo.category.Category;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.tool.Condition;
import com.example.demo.tool.Tool;
import com.example.demo.tool.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ToolDocumentMapperTest {

    @InjectMocks
    private ToolDocumentMapper toolDocumentMapper;

    @Test
    public void toDocument_returnCorrectlyDocument() {
        Tool tool = Tool.builder()
                .id(1L)
                .manufacturer(Manufacturer.builder().id(1L).build())
                .category(Category.builder().id(1L).build())
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(BigDecimal.valueOf(3000))
                .description("Some description")
                .build();

        ToolDocument result = toolDocumentMapper.toDocument(tool);

        assertNotNull(result);
        assertEquals(tool.getId(), result.getId());
        assertEquals(tool.getManufacturer().getId(), result.getManufacturer());
        assertEquals(tool.getCategory().getId(), result.getCategory());
        assertEquals(tool.getType().name(), result.getType());
        assertEquals(tool.getCondition().name(), result.getCondition());
        assertEquals(tool.getDescription(), result.getDescription());
    }
  
}