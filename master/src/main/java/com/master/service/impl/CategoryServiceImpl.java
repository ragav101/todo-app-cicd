package com.master.service.impl;

import com.master.dao.CategoryDao;
import com.master.dto.CategoryRequestDto;
import com.master.dto.CategoryResponseDto;
import com.master.entity.Category;
import com.master.exception.ApplicationException;
import com.master.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public CategoryResponseDto create(CategoryRequestDto request) {
        if (categoryDao.existsByNameIgnoreCase(request.name())) {
            throw new ApplicationException("Category already exists: " + request.name(), HttpStatus.CONFLICT);
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return toResponse(categoryDao.save(category));
    }

    @Override
    public CategoryResponseDto getById(Long id) {
        return toResponse(findActive(id));
    }

    @Override
    public List<CategoryResponseDto> getAll() {
        return categoryDao.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        Category category = findActive(id);
        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(categoryDao.save(category));
    }

    @Override
    public void delete(Long id) {
        Category category = findActive(id);
        category.setActive(false);
        categoryDao.save(category);
    }

    private Category findActive(Long id) {
        return categoryDao.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("Category not found: " + id, HttpStatus.NOT_FOUND));
    }

    private CategoryResponseDto toResponse(Category c) {
        return new CategoryResponseDto(c.getId(), c.getName(), c.getDescription(),
                c.isActive(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
