package com.example.demo.tool;

import com.example.demo.category.Category;
import com.example.demo.category.CategoryMapper;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerMapper;
import com.example.demo.minio.MinioProperties;
import com.example.demo.minio.MinioService;
import com.example.demo.user.User;
import com.example.demo.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ToolMapper {

    private final UserMapper userMapper;
    private final ManufacturerMapper manufacturerMapper;
    private final CategoryMapper categoryMapper;
    private final MinioService minioService;
    private final MinioProperties minioProperties;

    public ToolDto toDto(Tool tool) {
        return ToolDto.builder()
                .id(tool.getId())
                .owner(userMapper.toDto(tool.getOwner()))
                .manufacturer(manufacturerMapper.toDto(tool.getManufacturer()))
                .category(categoryMapper.toDto(tool.getCategory()))
                .type(tool.getType())
                .condition(tool.getCondition())
                .price(tool.getPrice())
                .description(tool.getDescription())
                .photos(tool.getPhotos().stream()
                        .map(el -> minioService.getPresignedUrl(el, minioProperties.getBucket()))
                        .toList())
                .createdAt(tool.getCreatedAt())
                .updatedAt(tool.getUpdatedAt())
                .build();
    }

    public Tool toEntity(ToolCreateUpdateDto toolCreateUpdateDto, User user, Manufacturer manufacturer, Category category, List<String> fileNames, LocalDateTime now) {
        return Tool.builder()
                .owner(user)
                .manufacturer(manufacturer)
                .category(category)
                .type(toolCreateUpdateDto.getType())
                .condition(toolCreateUpdateDto.getCondition())
                .price(toolCreateUpdateDto.getPrice())
                .description(toolCreateUpdateDto.getDescription())
                .photos(fileNames)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

}
