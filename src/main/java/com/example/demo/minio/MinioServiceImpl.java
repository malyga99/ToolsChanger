package com.example.demo.minio;

import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.MinIoException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "minio")
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioServiceImpl.class);

    @Override
    public void uploadFile(String fileName, String bucketName, InputStream inputStream) {
        try {
            LOGGER.debug("Uploading file: {} to bucket: {}", fileName, bucketName);
            minioClient.putObject(PutObjectArgs.builder()
                    .stream(inputStream, inputStream.available(), -1)
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            LOGGER.debug("File: {} uploaded successfully to bucket: {}", fileName, bucketName);
        } catch (Exception e) {
            throw new FileUploadException("File upload exception: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(key = "'bucket_' + #bucketName + '_file' + #fileName")
    public void removeFile(String fileName, String bucketName) {
        try {
            LOGGER.debug("Deleting file: {} in bucket: {}", fileName, bucketName);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            LOGGER.debug("File: {} deleted successfully in bucket: {}", fileName, bucketName);
        } catch (Exception e) {
            throw new MinIoException("MinIo exception: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(key = "'bucket_' + #bucketName + '_file' + #fileName")
    public String getPresignedUrl(String fileName, String bucketName) {
        try {
            LOGGER.debug("Generating presigned URL for file: {} in bucket: {}", fileName, bucketName);
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .method(Method.GET)
                    .expiry(86400)
                    .build());
            LOGGER.debug("Generated presigned URL for file: {} in bucket: {}", fileName, bucketName);

            url = url.replace(minioProperties.getEndpoint(), minioProperties.getProxyUrl());
            return url;
        } catch (Exception e) {
            throw new MinIoException("MinIo exception: " + e.getMessage(), e);
        }
    }
}

