package com.example.demo.minio;

import com.example.demo.exception.MinIoException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer implements CommandLineRunner {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioInitializer.class);

    @Override
    public void run(String... args) throws Exception {
        createBucket(minioProperties.getBucket());
    }

    private void createBucket(String bucketName) {
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!isExist) {
                LOGGER.debug("Creating bucket: {}", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                LOGGER.debug("Bucket: {} created successfully", bucketName);
            }
        } catch (Exception e) {
            throw new MinIoException("MinIo exception: " + e.getMessage(), e);
        }
    }
}

