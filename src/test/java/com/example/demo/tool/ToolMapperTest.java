package com.example.demo.tool;

import com.example.demo.category.Category;
import com.example.demo.category.CategoryDto;
import com.example.demo.category.CategoryMapper;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerDto;
import com.example.demo.manufacturer.ManufacturerMapper;
import com.example.demo.minio.MinioProperties;
import com.example.demo.minio.MinioService;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ToolMapperTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ManufacturerMapper manufacturerMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MinioService minioService;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private ToolMapper toolMapper;

    @Test
    public void toDto_returnCorrectlyDto() {
        User owner = User.builder().firstname("Ivan").build();
        Manufacturer manufacturer = Manufacturer.builder().id(1L).build();
        Category category = Category.builder().id(1L).build();
        Tool tool = Tool.builder()
                .id(1L)
                .owner(owner)
                .manufacturer(manufacturer)
                .category(category)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(BigDecimal.valueOf(100))
                .description("Some description")
                .photos(List.of("photo1.jpg", "photo2.jpg"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(minioProperties.getBucket()).thenReturn("test-bucket");
        when(userMapper.toDto(owner)).thenReturn(UserDto.builder().firstname("Ivan").build());
        when(manufacturerMapper.toDto(manufacturer)).thenReturn(ManufacturerDto.builder().id(1L).build());
        when(categoryMapper.toDto(category)).thenReturn(CategoryDto.builder().id(1L).build());
        when(minioService.getPresignedUrl("photo1.jpg", "test-bucket")).thenReturn("http://minio/photo1.jpg");
        when(minioService.getPresignedUrl("photo2.jpg", "test-bucket")).thenReturn("http://minio/photo2.jpg");

        ToolDto result = toolMapper.toDto(tool);

        assertNotNull(result);
        assertEquals(tool.getId(), result.getId());
        assertEquals(owner.getFirstname(), result.getOwner().getFirstname());
        assertEquals(manufacturer.getId(), result.getManufacturer().getId());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(tool.getType(), result.getType());
        assertEquals(tool.getCondition(), result.getCondition());
        assertEquals(tool.getPrice(), result.getPrice());
        assertEquals(tool.getDescription(), result.getDescription());
        assertNotNull(tool.getCreatedAt());
        assertNotNull(tool.getUpdatedAt());
        assertEquals("http://minio/photo1.jpg", result.getPhotos().get(0));
        assertEquals("http://minio/photo2.jpg", result.getPhotos().get(1));

        verify(minioService, times(1)).getPresignedUrl("photo1.jpg", "test-bucket");
        verify(minioService, times(1)).getPresignedUrl("photo2.jpg", "test-bucket");
    }

    @Test
    public void toEntity_returnCorrectlyEntity() {
        User owner = User.builder().firstname("Ivan").build();
        Manufacturer manufacturer = Manufacturer.builder().id(1L).build();
        Category category = Category.builder().id(1L).build();
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(BigDecimal.valueOf(3000))
                .description("Some description")
                .build();
        List<String> fileNames = List.of("photo1.jpg", "photo2.jpg");

        Tool result = toolMapper.toEntity(toolCreateUpdateDto, owner, manufacturer, category, fileNames, LocalDateTime.now());

        assertNotNull(result);
        assertEquals(owner.getFirstname(), result.getOwner().getFirstname());
        assertEquals(manufacturer.getName(), result.getManufacturer().getName());
        assertEquals(category.getName(), result.getCategory().getName());
        assertEquals(toolCreateUpdateDto.getType(), result.getType());
        assertEquals(toolCreateUpdateDto.getCondition(), result.getCondition());
        assertEquals(toolCreateUpdateDto.getPrice(), result.getPrice());
        assertEquals(toolCreateUpdateDto.getDescription(), result.getDescription());
        assertEquals(fileNames, result.getPhotos());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

}