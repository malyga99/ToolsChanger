package com.example.demo.minio;

import com.example.demo.exception.MinIoException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioInitializerTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioInitializer minioInitializer;

    @Test
    public void createBucket_createsBucket() throws Exception {
        ArgumentCaptor<MakeBucketArgs> argumentCaptor = ArgumentCaptor.forClass(MakeBucketArgs.class);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioProperties.getBucket()).thenReturn("test-bucket");

        minioInitializer.run();

        verify(minioClient, times(1)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, times(1)).makeBucket(argumentCaptor.capture());

        MakeBucketArgs makeBucketArgs = argumentCaptor.getValue();
        assertEquals("test-bucket", makeBucketArgs.bucket());
    }

    @Test
    public void createBucket_ifExists_DoesNotCreateBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioProperties.getBucket()).thenReturn("test-bucket");

        minioInitializer.run();

        verify(minioClient, times(1)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    public void createBucket_ifFailed_throwExc() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        when(minioProperties.getBucket()).thenReturn("test-bucket");
        doThrow(new RuntimeException("Make bucket failed")).when(minioClient).makeBucket(any(MakeBucketArgs.class));

        MinIoException minioException = assertThrows(MinIoException.class, () -> minioInitializer.run());
        assertEquals("MinIo exception: Make bucket failed", minioException.getMessage());
    }

}