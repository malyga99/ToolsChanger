package com.example.demo.tool;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ToolService {

    ToolDto create (ToolCreateUpdateDto toolCreateUpdateDto, List<MultipartFile> files);

    Page<ToolDto> findAll(Pageable pageable);

    Page<ToolDto> findMy(Pageable pageable);

    void delete(Long id);

    void update(Long id, ToolCreateUpdateDto toolCreateUpdateDto, List<MultipartFile> files, List<String> filesToDelete);

    Page<ToolDto> search(String description, Long manufacturer, Long category, String type, String condition, BigDecimal gte, BigDecimal lte, Pageable pageable);

    ToolDto findById(Long id);
}
