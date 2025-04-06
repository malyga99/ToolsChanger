package com.example.demo.category;

import com.example.demo.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public List<CategoryDto> findAll() {
        LOGGER.debug("findAll: Fetching categories");
        List<Category> categories = categoryRepository.findAll();
        LOGGER.debug("findAll: Fetched {} categories", categories.size());
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();

    }

    @Override
    public Category findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id: " + id + " not found"));
        LOGGER.debug("findById: Fetched category by id: {}", id);
        return category;
    }

}
