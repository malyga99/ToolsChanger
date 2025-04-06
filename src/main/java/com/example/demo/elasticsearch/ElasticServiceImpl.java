package com.example.demo.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demo.exception.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticServiceImpl implements ElasticService {

    private final ElasticProperties elasticProperties;
    private final ElasticsearchClient elasticsearchClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticServiceImpl.class);

    @Override
    public void save(ToolDocument toolDocument) {
        String index = elasticProperties.getIndex();
        LOGGER.debug("Saving document with id: {} to index: {}", toolDocument.getId(), index);
        IndexRequest<ToolDocument> request = new IndexRequest.Builder<ToolDocument>()
                .index(index)
                .id(String.valueOf(toolDocument.getId()))
                .document(toolDocument)
                .build();
        try {
            elasticsearchClient.index(request);
            LOGGER.debug("Successfully saved document with id: {} to index: {}", toolDocument.getId(), index);
        } catch (IOException e) {
            throw new ElasticsearchException("Elastic search exception: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long toolId) {
        String index = elasticProperties.getIndex();
        LOGGER.debug("Deleting document with id: {} from index: {}", toolId, index);
        DeleteRequest request = new DeleteRequest.Builder()
                .index(index)
                .id(String.valueOf(toolId))
                .build();
        try {
            elasticsearchClient.delete(request);
            LOGGER.debug("Successfully deleted document with id: {} from index: {}", toolId, index);
        } catch (IOException e) {
            throw new ElasticsearchException("Elastic search exception: " + e.getMessage(), e);
        }

    }

    @Override
    public List<Long> search(String description, Long manufacturer, Long category, String type, String condition, BigDecimal gte, BigDecimal lte) {
        String index = elasticProperties.getIndex();
        LOGGER.debug("Searching documents with filters - description: {}, manufacturer: {}, category: {}, type: {}, condition: {}, price range: {} - {} from index: {}",
                description, manufacturer, category, type, condition, gte, lte, index);
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(index)
                .query(q -> q.bool(b -> {
                    if (description != null && !description.isBlank()) {
                        b.must(m -> m.match(m1 -> m1.field("description").query(description)));
                    }
                    if (manufacturer != null) {
                        b.filter(f -> f.term(t -> t.field("manufacturer").value(manufacturer)));
                    }
                    if (category != null) {
                        b.filter(f -> f.term(t -> t.field("category").value(category)));
                    }
                    if (type != null) {
                        b.filter(f -> f.term(t -> t.field("type").value(type)));
                    }
                    if (condition != null) {
                        b.filter(f -> f.term(t -> t.field("condition").value(condition)));
                    }
                    if (gte != null && lte != null) {
                        b.filter(f -> f.range(r -> r.number(n -> n
                                .field("price")
                                .gte(gte.doubleValue())
                                .lte(lte.doubleValue())
                        )));
                    }
                    return b;
                }))
                .build();

        SearchResponse<Void> response = null;
        try {
            response = elasticsearchClient.search(searchRequest, Void.class);
            LOGGER.debug("Successfully received: {} documents from index: {}", response.hits().hits().size(), index);
        } catch (IOException e) {
            throw new ElasticsearchException("Elastic search exception: " + e.getMessage(), e);
        }


        List<Long> ids = response.hits().hits().stream()
                .map(el -> Long.valueOf(el.id()))
                .toList();

        return ids;
    }
}
