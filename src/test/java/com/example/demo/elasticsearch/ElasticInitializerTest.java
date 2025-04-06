package com.example.demo.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.example.demo.exception.ElasticsearchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ElasticInitializerTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private ElasticProperties elasticProperties;

    @Mock
    private ElasticsearchIndicesClient elasticsearchIndicesClient;

    @InjectMocks
    private ElasticInitializer elasticInitializer;

    @Test
    public void createIndex_createsIndex() throws Exception {
        ArgumentCaptor<CreateIndexRequest> argumentCaptor = ArgumentCaptor.forClass(CreateIndexRequest.class);
        when(elasticProperties.getIndex()).thenReturn("toolsindex");
        when(elasticsearchClient.indices()).thenReturn(elasticsearchIndicesClient);
        when(elasticsearchIndicesClient.exists(any(ExistsRequest.class))).thenReturn(new BooleanResponse(false));

        elasticInitializer.run();

        verify(elasticsearchIndicesClient, times(1)).exists(any(ExistsRequest.class));
        verify(elasticsearchIndicesClient, times(1)).create(argumentCaptor.capture());

        CreateIndexRequest createIndexRequest = argumentCaptor.getValue();
        assertEquals("toolsindex", createIndexRequest.index());
    }

    @Test
    public void createIndex_ifExists_doesNotCreateIndex() throws Exception {
        when(elasticProperties.getIndex()).thenReturn("toolsindex");
        when(elasticsearchClient.indices()).thenReturn(elasticsearchIndicesClient);
        when(elasticsearchIndicesClient.exists(any(ExistsRequest.class))).thenReturn(new BooleanResponse(true));

        elasticInitializer.run();

        verify(elasticsearchIndicesClient, times(1)).exists(any(ExistsRequest.class));
        verify(elasticsearchIndicesClient, never()).create(any(CreateIndexRequest.class));

    }

    @Test
    public void createIndex_ifFailed_throwExc() throws Exception{
        when(elasticProperties.getIndex()).thenReturn("toolsindex");
        when(elasticsearchClient.indices()).thenReturn(elasticsearchIndicesClient);
        when(elasticsearchIndicesClient.exists(any(ExistsRequest.class))).thenThrow(new IOException("IO exception"));

        ElasticsearchException elasticsearchException = assertThrows(ElasticsearchException.class, () -> elasticInitializer.run());
        assertEquals("Elastic search exception: IO exception", elasticsearchException.getMessage());
    }


}