package com.master.service;

import com.master.dto.CategoryRequestDto;
import com.master.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    CategoryResponseDto create(CategoryRequestDto request);
    CategoryResponseDto getById(Long id);
    List<CategoryResponseDto> getAll();
    CategoryResponseDto update(Long id, CategoryRequestDto request);
    void delete(Long id);
}
