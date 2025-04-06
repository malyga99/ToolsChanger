package com.example.demo.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.example.demo.exception.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ElasticInitializer implements CommandLineRunner {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticProperties elasticProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticInitializer.class);

    @Override
    public void run(String... args) throws Exception {
        createIndex(elasticProperties.getIndex());
    }

    private void createIndex(String indexName) {
        try {
            BooleanResponse isExist = elasticsearchClient.indices().exists(new ExistsRequest.Builder()
                    .index(elasticProperties.getIndex())
                    .build());
            if (!isExist.value()) {
                LOGGER.debug("Creating index: {}", indexName);
                try (InputStream inputStream = ElasticInitializer.class.getClassLoader().getResourceAsStream("elasticsearch/tool-mapping.json")) {
                    elasticsearchClient.indices().create(new CreateIndexRequest.Builder()
                            .index(indexName)
                            .withJson(inputStream)
                            .build());
                    LOGGER.debug("Index: {} created successfully", indexName);
                }
            }
        } catch (IOException e) {
            throw new ElasticsearchException("Elastic search exception: " + e.getMessage(), e);
        }
    }
}
