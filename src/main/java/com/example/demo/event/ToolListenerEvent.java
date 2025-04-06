package com.example.demo.event;

import com.example.demo.elasticsearch.ElasticService;
import com.example.demo.elasticsearch.ToolDocument;
import com.example.demo.elasticsearch.ToolDocumentMapper;
import com.example.demo.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ToolListenerEvent {

    private final ElasticService elasticService;
    private final ToolDocumentMapper toolDocumentMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolListenerEvent.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleToolCreated(ToolCreatedEvent toolCreatedEvent) {
        Tool createdTool = toolCreatedEvent.getCreatedTool();

        LOGGER.debug("Received ToolCreatedEvent for tool id: {}", createdTool.getId());
        ToolDocument toolDocument = toolDocumentMapper.toDocument(createdTool);

        elasticService.save(toolDocument);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleToolUpdated(ToolUpdatedEvent toolUpdatedEvent) {
        Tool updatedTool = toolUpdatedEvent.getUpdatedTool();

        LOGGER.debug("Received ToolUpdatedEvent for tool id: {}", updatedTool.getId());
        ToolDocument toolDocument = toolDocumentMapper.toDocument(updatedTool);

        elasticService.save(toolDocument);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleToolDeleted(ToolDeletedEvent toolDeletedEvent) {
        Long toolId = toolDeletedEvent.getToolId();

        LOGGER.debug("Received ToolDeletedEvent for tool id: {}", toolId);
        elasticService.delete(toolId);
    }
}
