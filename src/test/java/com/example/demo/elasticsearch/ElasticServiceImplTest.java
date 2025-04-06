package com.example.demo.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.example.demo.exception.ElasticsearchException;
import com.example.demo.tool.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ElasticServiceImplTest {

    @Mock
    private ElasticProperties elasticProperties;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private ElasticServiceImpl elasticService;

    private ToolDocument toolDocument;

    @BeforeEach
    public void setup() {
        toolDocument = ToolDocument.builder()
                .id(1L)
                .manufacturer(1L)
                .category(1L)
                .type("RENT")
                .condition("NEW")
                .price(new BigDecimal("3000"))
                .description("Some description")
                .build();
    }

    @Test
    public void save_savesDocument() throws IOException {
        ArgumentCaptor<IndexRequest<ToolDocument>> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);
        when(elasticProperties.getIndex()).thenReturn("toolsindex");

        elasticService.save(toolDocument);

        verify(elasticsearchClient, times(1)).index(argumentCaptor.capture());

        IndexRequest<ToolDocument> indexRequest = argumentCaptor.getValue();
        assertEquals("toolsindex", indexRequest.index());
        assertEquals("1", indexRequest.id());
        assertEquals(toolDocument, indexRequest.document());
    }

    @Test
    public void save_ifFailed_throwExc() throws IOException {
        when(elasticProperties.getIndex()).thenReturn("toolsindex");
        doThrow(new IOException("IO Exception")).when(elasticsearchClient).index(any(IndexRequest.class));

        ElasticsearchException elasticsearchException = assertThrows(ElasticsearchException.class, () -> elasticService.save(toolDocument));
        assertEquals("Elastic search exception: IO Exception", elasticsearchException.getMessage());
    }

    @Test
    public void search_returnTwoIds() throws IOException {
        Hit<Void> hit1 = new Hit.Builder<Void>().id("1").index("toolsindex").build();
        Hit<Void> hit2 = new Hit.Builder<Void>().id("2").index("toolsindex").build();
        SearchResponse<Void> searchResponse = mock(SearchResponse.class);
        List<Hit<Void>> hits = List.of(hit1, hit2);
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(searchResponse);
        when(searchResponse.hits()).thenReturn(new HitsMetadata.Builder<Void>()
                .hits(hits)
                .build());
        when(elasticProperties.getIndex()).thenReturn("toolsindex");

        List<Long> result = elasticService.search("description", 1L, 1L, "EXCHANGE", "RENT", new BigDecimal("1000"), new BigDecimal("3000"));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0));
        assertEquals(2L, result.get(1));

        verify(elasticsearchClient, times(1)).search(any(SearchRequest.class), eq(Void.class));
    }

    @Test
    public void search_passCorrectSearchRequest() throws IOException {
        ArgumentCaptor<SearchRequest> searchRequestArgumentCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        SearchResponse<Void> searchResponse = mock(SearchResponse.class);
        List<Hit<Void>> hits = List.of();
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(searchResponse);
        when(searchResponse.hits()).thenReturn(new HitsMetadata.Builder<Void>()
                .hits(hits)
                .build());
        when(elasticProperties.getIndex()).thenReturn("toolsindex");

        elasticService.search("description", 1L, 1L, "EXCHANGE", "RENT", new BigDecimal("1000"), new BigDecimal("3000"));

        verify(elasticsearchClient, times(1)).search(searchRequestArgumentCaptor.capture(), eq(Void.class));

        SearchRequest searchRequest = searchRequestArgumentCaptor.getValue();
        BoolQuery boolQuery = searchRequest.query().bool();

        assertEquals("toolsindex", searchRequest.index().get(0));
        assertTrue(boolQuery.must().stream().anyMatch(
                q -> q.match().field().equals("description") && q.match().query().stringValue().equals("description")
        ));
        assertTrue(boolQuery.filter().stream().anyMatch(
                q -> q.term().field().equals("manufacturer") && q.term().value().longValue() == 1
        ));
        assertTrue(boolQuery.filter().stream().anyMatch(
                q -> q.term().field().equals("category") && q.term().value().longValue() == 1
        ));
        assertTrue(boolQuery.filter().stream().anyMatch(
                q -> q.term().field().equals("type") && q.term().value().stringValue().equals("EXCHANGE")
        ));
        assertTrue(boolQuery.filter().stream().anyMatch(
                q -> q.term().field().equals("condition") && q.term().value().stringValue().equals("RENT")
        ));
        assertTrue(boolQuery.filter().stream().anyMatch(
                q -> q.isRange() &&
                        q.range().number().field().equals("price") &&
                        q.range().number().gte().doubleValue() == 1000.0 &&
                        q.range().number().lte().doubleValue() == 3000.0
        ));
    }

    @Test
    public void search_withoutParameter_dontIncludeInRequest() throws IOException {
        ArgumentCaptor<SearchRequest> searchRequestArgumentCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        SearchResponse<Void> searchResponse = mock(SearchResponse.class);
        List<Hit<Void>> hits = List.of();
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(searchResponse);
        when(searchResponse.hits()).thenReturn(new HitsMetadata.Builder<Void>()
                .hits(hits)
                .build());
        when(elasticProperties.getIndex()).thenReturn("toolsindex");

        elasticService.search(null,1L, 1L, "EXCHANGE", "RENT", new BigDecimal("1000"), new BigDecimal("3000"));

        verify(elasticsearchClient, times(1)).search(searchRequestArgumentCaptor.capture(), eq(Void.class));

        SearchRequest searchRequest = searchRequestArgumentCaptor.getValue();
        BoolQuery boolQuery = searchRequest.query().bool();

        assertTrue(boolQuery.must().stream().noneMatch(
                q -> q.match().field().equals("description")
        ));
        assertTrue(boolQuery.filter().stream().anyMatch(
                q -> q.term().field().equals("type") && q.term().value().stringValue().equals("EXCHANGE")
        ));
    }

    @Test
    public void search_ifFailed_throwExc() throws IOException {
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenThrow(new IOException("IO Exception"));
        when(elasticProperties.getIndex()).thenReturn("toolsindex");

        ElasticsearchException elasticsearchException = assertThrows(ElasticsearchException.class, () -> elasticService.search("description", 1L, 1L, "EXCHANGE", "NEW", new BigDecimal("1000"), new BigDecimal("3000")));
        assertEquals("Elastic search exception: IO Exception", elasticsearchException.getMessage());
    }

    @Test
    public void delete_deletesDocument() throws IOException {
        ArgumentCaptor<DeleteRequest> argumentCaptor = ArgumentCaptor.forClass(DeleteRequest.class);
        when(elasticProperties.getIndex()).thenReturn("toolsindex");

        elasticService.delete(1L);

        verify(elasticsearchClient, times(1)).delete(argumentCaptor.capture());

        DeleteRequest deleteRequest = argumentCaptor.getValue();
        assertEquals("toolsindex", deleteRequest.index());
        assertEquals("1", deleteRequest.id());
    }

    @Test
    public void delete_ifFailed_throwExc() throws IOException {
        when(elasticProperties.getIndex()).thenReturn("toolsindex");
        doThrow(new IOException("IO Exception")).when(elasticsearchClient).delete(any(DeleteRequest.class));

        ElasticsearchException elasticsearchException = assertThrows(ElasticsearchException.class, () -> elasticService.delete(1L));
        assertEquals("Elastic search exception: IO Exception", elasticsearchException.getMessage());
    }

}