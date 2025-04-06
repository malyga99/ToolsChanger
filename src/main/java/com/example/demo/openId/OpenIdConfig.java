package com.example.demo.openId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenIdConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
