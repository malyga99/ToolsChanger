package com.example.demo.category;

import java.util.List;

public interface CategoryService {
    Category findById(Long id);

    List<CategoryDto> findAll();
}
