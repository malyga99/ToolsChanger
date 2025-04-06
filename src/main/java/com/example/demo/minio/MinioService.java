package com.example.demo.minio;

import java.io.InputStream;

public interface MinioService {
    void uploadFile(String fileName, String bucketName, InputStream inputStream);

    void removeFile(String fileName, String bucketName);

    String getPresignedUrl(String fileName, String bucketName);
}
