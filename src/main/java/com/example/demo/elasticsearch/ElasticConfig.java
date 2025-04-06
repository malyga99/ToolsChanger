package com.example.demo.elasticsearch;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
@Configuration
@RequiredArgsConstructor
public class ElasticConfig extends ElasticsearchConfiguration {

    private final ElasticProperties elasticProperties;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticProperties.getUrl())
                .withBasicAuth(elasticProperties.getUsername(), elasticProperties.getPassword())
                .build();
    }
}
