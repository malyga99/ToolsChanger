package com.example.demo.category;

import com.example.demo.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryDto firstCategoryDto;

    private CategoryDto secondCategoryDto;

    private Category firstCategory;

    private Category secondCategory;

    @BeforeEach
    public void setup() {
        firstCategoryDto = CategoryDto.builder()
                .id(1L)
                .name("Hammer")
                .build();
        secondCategoryDto = CategoryDto.builder()
                .id(2L)
                .name("Drill")
                .build();
        firstCategory = Category.builder()
                .id(1L)
                .name("Hammer")
                .build();
        secondCategory = Category.builder()
                .id(2L)
                .name("Drill")
                .build();
    }

    @Test
    public void findAll_returnTwoCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(firstCategory, secondCategory));
        when(categoryMapper.toDto(firstCategory)).thenReturn(firstCategoryDto);
        when(categoryMapper.toDto(secondCategory)).thenReturn(secondCategoryDto);

        List<CategoryDto> result = categoryService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(firstCategoryDto, result.get(0));
        assertEquals(secondCategoryDto, result.get(1));
    }

    @Test
    public void findById_returnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(firstCategory));

        Category result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals(firstCategory, result);

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void findById_categoryNotFound_throwExc() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        CategoryNotFoundException categoryNotFoundException = assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(1L));

        assertEquals("Category with id: 1 not found", categoryNotFoundException.getMessage());

        verify(categoryRepository, times(1)).findById(1L);
    }

}