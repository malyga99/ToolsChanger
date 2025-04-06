package com.example.demo.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    String uploadAndGetFileName(MultipartFile file);

    String generateFileName(String originalFileName);

    List<String> processFiles(List<MultipartFile> files);

    void deleteFiles(List<String> fileNames);
}
