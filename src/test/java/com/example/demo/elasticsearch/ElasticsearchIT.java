package com.example.demo.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.example.demo.BaseIT;
import com.example.demo.exception.ElasticsearchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ElasticsearchIT extends BaseIT {

    @Autowired
    private ElasticProperties elasticProperties;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticInitializer elasticInitializer;

    @Autowired
    private ElasticService elasticService;

    private ToolDocument toolDocument;

    @BeforeEach
    public void setup() {
        cleanElastic();
        toolDocument = ToolDocument.builder()
                .id(1L)
                .description("High-quality drill")
                .manufacturer(100L)
                .category(200L)
                .type("EXCHANGE")
                .condition("NEW")
                .price(BigDecimal.valueOf(150.00))
                .build();
    }

    @Test
    public void save_saveDocumentCorrectly() {
        elasticService.save(toolDocument);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 1);

        assertTrue(isDocumentExistsInElastic(toolDocument.getId()));
    }

    @Test
    public void delete_deleteDocumentCorrectly() {
        elasticService.save(toolDocument);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 1);

        elasticService.delete(toolDocument.getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 0);

        assertFalse(isDocumentExistsInElastic(toolDocument.getId()));
    }

    @Test
    public void search_returnCorrectResults() {
        elasticService.save(toolDocument);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 1);

        List<Long> result = elasticService.search("High drill", 100L, 200L, "EXCHANGE", "NEW", BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(toolDocument.getId(), result.get(0));
    }

    @Test
    public void search_withoutFilters_returnAllDocuments() {
        elasticService.save(toolDocument);
        ToolDocument anotherTool = ToolDocument.builder()
                .id(2L)
                .description("Screwdriver")
                .manufacturer(101L)
                .category(201L)
                .type("EXCHANGE")
                .condition("USED")
                .price(BigDecimal.valueOf(50.00))
                .build();
        elasticService.save(anotherTool);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 2);

        List<Long> result = elasticService.search(null, null, null, null, null, null, null);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(toolDocument.getId(), result.get(0));
        assertEquals(anotherTool.getId(), result.get(1));
    }

    @Test
    public void search_withPriceRange_returnMatchingDocuments() {
        elasticService.save(toolDocument);
        ToolDocument cheapTool = ToolDocument.builder()
                .id(3L)
                .description("Cheap tool")
                .manufacturer(102L)
                .category(202L)
                .type("Hand Tool")
                .condition("New")
                .price(BigDecimal.valueOf(20.00))
                .build();
        elasticService.save(cheapTool);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
                elasticsearchClient.count(c -> c.index(elasticProperties.getIndex())).count() == 2);

        List<Long> result = elasticService.search(null, null, null, null, null, BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(toolDocument.getId(), result.get(0));
    }

    private boolean isDocumentExistsInElastic(Long toolID) {
        GetResponse<ToolDocument> getResponse = null;
        try {
            getResponse = elasticsearchClient.get(g -> g
                            .index(elasticProperties.getIndex())
                            .id(String.valueOf(toolID)),
                    ToolDocument.class
            );
        } catch (IOException e) {
            throw new ElasticsearchException("Get document failed: " + e.getMessage(), e);
        }
        return getResponse.found();
    }

    private void cleanElastic() {
        String index = elasticProperties.getIndex();

        try {
            elasticsearchClient.indices().delete(d -> d.index(index));
        } catch (IOException e) {
            throw new ElasticsearchException("Delete index failed:" + e.getMessage(), e);
        }

        try {
            elasticInitializer.run();
        } catch (Exception e) {
            throw new ElasticsearchException("Create index failed: " + e.getMessage(), e);
        }

    }
}
