package com.example.demo.minio;

import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.MinIoException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioServiceImpl minioService;

    @Test
    public void uploadFile_uploadsFile() throws Exception {
        ArgumentCaptor<PutObjectArgs> argumentCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        minioService.uploadFile("test-file.jpg", "test-bucket", new ByteArrayInputStream("dummy data".getBytes()));

        verify(minioClient, times(1)).putObject(argumentCaptor.capture());

        PutObjectArgs putObjectArgs = argumentCaptor.getValue();
        assertEquals("test-file.jpg", putObjectArgs.object());
        assertEquals("test-bucket", putObjectArgs.bucket());
        assertEquals("dummy data", new String(putObjectArgs.stream().readAllBytes()));
    }

    @Test
    public void uploadFile_ifFailed_throwExc() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("Upload file failed"));

        FileUploadException fileUploadException = assertThrows(FileUploadException.class, () -> minioService.uploadFile("test-file.jpg", "test-bucket", new ByteArrayInputStream("dummy data".getBytes())));
        assertEquals("File upload exception: Upload file failed", fileUploadException.getMessage());
    }

    @Test
    public void removeFile_removesFile() throws Exception {
        ArgumentCaptor<RemoveObjectArgs> argumentCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        minioService.removeFile("test-file.jpg", "test-bucket");

        verify(minioClient, times(1)).removeObject(argumentCaptor.capture());

        RemoveObjectArgs removeObjectArgs = argumentCaptor.getValue();
        assertEquals("test-file.jpg", removeObjectArgs.object());
        assertEquals("test-bucket", removeObjectArgs.bucket());
    }

    @Test
    public void removeFile_ifFailed_throwExc() throws Exception {
        doThrow(new RuntimeException("Remove file failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        MinIoException minioException = assertThrows(MinIoException.class, () -> minioService.removeFile("test-file.jpg", "test-bucket"));
        assertEquals("MinIo exception: Remove file failed", minioException.getMessage());
    }

    @Test
    public void getPresignedUrl_returnPresignedUrls() throws Exception {
        ArgumentCaptor<GetPresignedObjectUrlArgs> argumentCaptor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn("http://toolsminio:9000/testbucket/test-file.jpg");
        when(minioProperties.getEndpoint()).thenReturn("http://toolsminio:9000");
        when(minioProperties.getProxyUrl()).thenReturn("http://10.3.34.38:80");

        String result = minioService.getPresignedUrl("test-file.jpg", "test-bucket");

        verify(minioClient, times(1)).getPresignedObjectUrl(argumentCaptor.capture());
        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = argumentCaptor.getValue();

        assertNotNull(result);
        assertEquals("http://10.3.34.38:80/testbucket/test-file.jpg", result);
        assertEquals("test-file.jpg", getPresignedObjectUrlArgs.object());
        assertEquals("test-bucket", getPresignedObjectUrlArgs.bucket());
        assertEquals(Method.GET, getPresignedObjectUrlArgs.method());
    }

    @Test
    public void getPresignedUrl_ifFailed_throwExc() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenThrow(new RuntimeException("Get presigned url failed"));

        MinIoException minioException = assertThrows(MinIoException.class, () -> minioService.getPresignedUrl("test-file.jpg", "test-bucket"));
        assertEquals("MinIo exception: Get presigned url failed", minioException.getMessage());
    }


}