package com.example.demo;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BaseIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @Container
    private static final ElasticsearchContainer elastic = new ElasticsearchContainer("elasticsearch:8.17.2")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "true")
            .withPassword("test_password");

    @Container
    private static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withUserName("test_user")
            .withPassword("test_password")
            .withCommand("server /data --console-address :9001");

    @Container
    private static final RedisContainer redis = new RedisContainer("redis/redis-stack:latest")
            .withExposedPorts(6379, 8001)
            .withEnv("REDIS_PASSWORD", "test_password");

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "test_user");
        registry.add("spring.datasource.password", () -> "test_password");

        registry.add("elasticsearch.url", elastic::getHttpHostAddress);
        registry.add("elasticsearch.username", () -> "elastic");
        registry.add("elasticsearch.password", () -> "test_password");
        registry.add("elasticsearch.index", () -> "test_index");

        registry.add("minio.endpoint", minio::getS3URL);
        registry.add("minio.access-key", () -> "test_user");
        registry.add("minio.secret-key", () -> "test_password");
        registry.add("minio.user", () -> "test_user");
        registry.add("minio.password", () -> "test_password");
        registry.add("minio.bucket", () -> "toolsbucket");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getRedisPort);
        registry.add("spring.data.redis.password", () -> "test_password");
        registry.add("spring.data.redis.username", () -> "default");
    }

}

