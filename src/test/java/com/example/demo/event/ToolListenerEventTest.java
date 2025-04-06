package com.example.demo.event;

import com.example.demo.category.Category;
import com.example.demo.elasticsearch.ElasticService;
import com.example.demo.elasticsearch.ToolDocument;
import com.example.demo.elasticsearch.ToolDocumentMapper;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.tool.Condition;
import com.example.demo.tool.Tool;
import com.example.demo.tool.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ToolListenerEventTest {

    @Mock
    private ElasticService elasticService;

    @Mock
    private ToolDocumentMapper toolDocumentMapper;

    @InjectMocks
    private ToolListenerEvent toolListenerEvent;

    private Tool tool;

    private ToolDocument toolDocument;

    @BeforeEach
    public void setup() {
        tool = Tool.builder()
                .id(1L)
                .manufacturer(Manufacturer.builder().id(1L).build())
                .category(Category.builder().id(1L).build())
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some description")
                .build();
        toolDocument = ToolDocument.builder()
                .id(1L)
                .manufacturer(1L)
                .category(1L)
                .type(Type.RENT.name())
                .condition(Condition.NEW.name())
                .price(new BigDecimal("3000"))
                .description("Some description")
                .build();
    }

    @Test
    public void handleToolCreated_handlesToolCreated() {
        when(toolDocumentMapper.toDocument(tool)).thenReturn(toolDocument);

        toolListenerEvent.handleToolCreated(ToolCreatedEvent.builder()
                .createdTool(tool)
                .build());

        verify(toolDocumentMapper, times(1)).toDocument(tool);
        verify(elasticService, times(1)).save(toolDocument);
    }

    @Test
    public void handleToolUpdated_handlesToolUpdated() {
        when(toolDocumentMapper.toDocument(tool)).thenReturn(toolDocument);

        toolListenerEvent.handleToolUpdated(ToolUpdatedEvent.builder()
                .updatedTool(tool)
                .build());

        verify(toolDocumentMapper, times(1)).toDocument(tool);
        verify(elasticService, times(1)).save(toolDocument);
    }

    @Test
    public void handleToolDeleted_handlesToolDeleted() {

        toolListenerEvent.handleToolDeleted(ToolDeletedEvent.builder()
                .toolId(1L)
                .build());

        verify(elasticService, times(1)).delete(1L);
    }

}