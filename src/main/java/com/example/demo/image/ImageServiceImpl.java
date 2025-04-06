package com.example.demo.image;

import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.FileValidationException;
import com.example.demo.minio.MinioProperties;
import com.example.demo.minio.MinioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final MinioProperties minioProperties;
    private final MinioService minioService;
    private final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Override
    public String uploadAndGetFileName(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            throw new FileValidationException("File is empty or does not contains a name");
        }
        LOGGER.debug("Attempting to upload file to storage: {}", file.getOriginalFilename());

        String fileName = generateFileName(file.getOriginalFilename());
        LOGGER.debug("Generated unique file name: {}", fileName);

        try (InputStream inputStream = file.getInputStream()) {
                minioService.uploadFile(fileName, minioProperties.getBucket(), inputStream);
        } catch (IOException e) {
            throw new FileUploadException("File upload exception: " + e.getMessage(), e);
        }

        LOGGER.debug("Successfully uploaded file: {}", fileName);
        return fileName;
    }

    @Override
    public String generateFileName(String originalFileName) {
        int lastDotIndex = originalFileName.lastIndexOf('.');
        String extension = (lastDotIndex != -1) ? originalFileName.substring(lastDotIndex + 1) : "unknown";

        return UUID.randomUUID() + "." + extension;
    }

    @Override
    public List<String> processFiles(List<MultipartFile> files) {
        return files.stream()
                .filter(el -> !el.isEmpty())
                .map(this::uploadAndGetFileName)
                .toList();
    }

    @Override
    public void deleteFiles(List<String> fileNames) {
        if (fileNames != null) {
            fileNames.forEach(el -> minioService.removeFile(el, minioProperties.getBucket()));
        }
    }

}
