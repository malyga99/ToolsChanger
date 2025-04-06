package com.example.demo.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioProperties {

    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String bucket;
    private String proxyUrl;
    private String user;
    private String password;
}
