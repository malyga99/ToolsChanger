package com.example.demo.minio;

import com.example.demo.BaseIT;
import com.example.demo.exception.MinIoException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class MinioIT extends BaseIT {

    private static final String FILE_NAME = "file.jpg";

    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioInitializer minioInitializer;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        cleanMinio();
    }

    @Test
    public void uploadFile_uploadFileSuccessfully() {
        initMinio();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("dummy".getBytes());
        minioService.uploadFile("test-file.jpg", minioProperties.getBucket(), inputStream);

        assertTrue(isFileExists("test-file.jpg"));
    }

    @Test
    public void getPresignedUrl_generatePresignedUrl() {
        initMinio();
        String presignedUrl = minioService.getPresignedUrl(FILE_NAME, minioProperties.getBucket());

        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.startsWith(minioProperties.getProxyUrl()));
    }

    @Test
    public void getPresignedUrl_cachePresignedUrlCorrectly() {
        initMinio();
        Cache cache = cacheManager.getCache("minio");
        assertNotNull(cache);

        long start = System.currentTimeMillis();
        String firstCall = minioService.getPresignedUrl(FILE_NAME, minioProperties.getBucket());
        long firstDuration = System.currentTimeMillis() - start;
        assertNotNull(firstCall);

        String cacheKey = "bucket_" + minioProperties.getBucket() + "_file" + FILE_NAME;
        Cache.ValueWrapper url = cache.get(cacheKey);
        assertNotNull(url);
        assertNotNull(url.get());

        start = System.currentTimeMillis();
        String secondCall = minioService.getPresignedUrl(FILE_NAME, minioProperties.getBucket());;
        long secondDuration = System.currentTimeMillis() - start;

        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);
        assertTrue(secondDuration < firstDuration / 2, "Get from cache");
    }

    @Test
    public void removeFile_removeFileSuccessfully() {
        initMinio();
        minioService.removeFile(FILE_NAME, minioProperties.getBucket());

        assertFalse(isFileExists(FILE_NAME));
    }

    @Test
    public void removeFile_deleteFromCacheCorrectly() {
        initMinio();
        Cache cache = cacheManager.getCache("minio");
        assertNotNull(cache);

        String cacheKey = "bucket_" + minioProperties.getBucket() + "_file" + FILE_NAME;
        minioService.getPresignedUrl(FILE_NAME, minioProperties.getBucket());
        Cache.ValueWrapper fromCache = cache.get(cacheKey);
        assertNotNull(fromCache);
        assertNotNull(fromCache.get());

        minioService.removeFile(FILE_NAME, minioProperties.getBucket());
        Cache.ValueWrapper fromCacheAfterRemove = cache.get(cacheKey);
        assertNull(fromCacheAfterRemove);
    }

    @SneakyThrows
    private void initMinio() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("dummy".getBytes());
        minioService.uploadFile(FILE_NAME, minioProperties.getBucket(), inputStream);
    }

    @SneakyThrows
    private void cleanMinio() {
        String bucketName = minioProperties.getBucket();
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (bucketExists) {
            Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());

            for (Result<Item> object : objects) {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(object.get().objectName())
                        .build());
            }

            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());

            minioInitializer.run();
        }
    }

    private boolean isFileExists(String fileName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {

            throw new MinIoException("Get stat file failed: " + e.getMessage(), e);
        }
    }
}
