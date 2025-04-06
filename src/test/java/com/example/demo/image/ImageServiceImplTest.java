package com.example.demo.image;

import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.FileValidationException;
import com.example.demo.minio.MinioProperties;
import com.example.demo.minio.MinioService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioService minioService;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    @Spy
    private ImageServiceImpl imageService;

    private MultipartFile firstFile;

    private MultipartFile secondFile;

    @BeforeEach
    public void setup() {
        firstFile = mock(MultipartFile.class);
        secondFile = mock(MultipartFile.class);
    }

    @Test
    public void uploadAndGetFileName_uploadAndReturnFileName() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("dummy data".getBytes());
        when(firstFile.getOriginalFilename()).thenReturn("test-file.jpg");
        when(minioProperties.getBucket()).thenReturn("test-bucket");
        doNothing().when(minioService).uploadFile(any(String.class), eq("test-bucket"), eq(inputStream));
        when(firstFile.getInputStream()).thenReturn(inputStream);

        String result = imageService.uploadAndGetFileName(firstFile);

        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"));

        verify(minioService, times(1)).uploadFile(any(String.class), eq("test-bucket"), eq(inputStream));
        verify(firstFile, times(1)).getInputStream();
    }

    @Test
    public void uploadAndGetFileName_failedToGetInputStream_throwExc() throws IOException {
        when(firstFile.getOriginalFilename()).thenReturn("test-file.jpg");
        when(firstFile.getInputStream()).thenThrow(new IOException("Stream error"));

        FileUploadException fileUploadException = assertThrows(FileUploadException.class, () -> imageService.uploadAndGetFileName(firstFile));
        assertEquals("File upload exception: Stream error", fileUploadException.getMessage());

        verify(minioService, never()).uploadFile(any(), any(), any());
    }

    @Test
    public void uploadAndGetFileName_withoutFile_throwExc() {
        FileValidationException fileValidationException = assertThrows(FileValidationException.class, () -> imageService.uploadAndGetFileName(null));
        assertEquals("File is empty or does not contains a name", fileValidationException.getMessage());

        verifyNoInteractions(minioClient);
    }

    @Test
    public void uploadAndGetFileName_withoutOriginalFileName_throwExc() {
        when(firstFile.getOriginalFilename()).thenReturn(null);

        FileValidationException fileValidationException = assertThrows(FileValidationException.class, () -> imageService.uploadAndGetFileName(firstFile));
        assertEquals("File is empty or does not contains a name", fileValidationException.getMessage());

        verifyNoInteractions(minioClient);
    }

    @Test
    public void generateFileName_returnFileNameWithExtension() {
        String result = imageService.generateFileName("test-file.jpg");

        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    public void generateFileName_returnFileNameWithoutExtension() {
        String result = imageService.generateFileName("test-file");

        assertNotNull(result);
        assertTrue(result.endsWith(".unknown"));
    }

    @Test
    public void processFiles_returnFileNames() {
        when(firstFile.isEmpty()).thenReturn(false);
        when(secondFile.isEmpty()).thenReturn(false);
        doReturn("file1.jpg").when(imageService).uploadAndGetFileName(firstFile);
        doReturn("file2.jpg").when(imageService).uploadAndGetFileName(secondFile);

        List<String> result = imageService.processFiles(List.of(firstFile, secondFile));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("file1.jpg", result.get(0));
        assertEquals("file2.jpg", result.get(1));

        verify(imageService, times(1)).uploadAndGetFileName(firstFile);
        verify(imageService, times(1)).uploadAndGetFileName(secondFile);
    }

    @Test
    public void processFiles_filesIsEmpty_returnEmptyList() {
        when(firstFile.isEmpty()).thenReturn(true);
        when(secondFile.isEmpty()).thenReturn(true);

        List<String> result = imageService.processFiles(List.of(firstFile, secondFile));

        assertEquals(0, result.size());
    }

    @Test
    public void deleteFiles_deleteFiles() {
        when(minioProperties.getBucket()).thenReturn("test-bucket");
        doNothing().when(minioService).removeFile("file1.jpg", "test-bucket");
        doNothing().when(minioService).removeFile("file2.jpg", "test-bucket");

        imageService.deleteFiles(List.of("file1.jpg", "file2.jpg"));

        verify(minioProperties, times(2)).getBucket();
        verify(minioService, times(1)).removeFile("file1.jpg", "test-bucket");
        verify(minioService, times(1)).removeFile("file2.jpg", "test-bucket");
    }

    @Test
    public void deleteFiles_ifNull_doNothing() {
        imageService.deleteFiles(null);

        verifyNoInteractions(minioProperties, minioService);
    }
}