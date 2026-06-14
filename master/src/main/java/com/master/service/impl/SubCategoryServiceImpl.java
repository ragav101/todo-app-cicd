package com.master.service.impl;

import com.master.dao.CategoryDao;
import com.master.dao.SubCategoryDao;
import com.master.dto.SubCategoryRequestDto;
import com.master.dto.SubCategoryResponseDto;
import com.master.entity.Category;
import com.master.entity.SubCategory;
import com.master.exception.ApplicationException;
import com.master.service.SubCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryDao subCategoryDao;
    private final CategoryDao categoryDao;

    public SubCategoryServiceImpl(SubCategoryDao subCategoryDao, CategoryDao categoryDao) {
        this.subCategoryDao = subCategoryDao;
        this.categoryDao = categoryDao;
    }

    @Override
    public SubCategoryResponseDto create(SubCategoryRequestDto request) {
        Category category = findActiveCategory(request.categoryId());
        if (subCategoryDao.existsByNameIgnoreCaseAndCategoryId(request.name(), request.categoryId())) {
            throw new ApplicationException("SubCategory already exists: " + request.name(), HttpStatus.CONFLICT);
        }
        SubCategory subCategory = SubCategory.builder()
                .name(request.name())
                .description(request.description())
                .category(category)
                .build();
        return toResponse(subCategoryDao.save(subCategory));
    }

    @Override
    public SubCategoryResponseDto getById(Long id) {
        return toResponse(findActive(id));
    }

    @Override
    public List<SubCategoryResponseDto> getAll() {
        return subCategoryDao.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Override
    public List<SubCategoryResponseDto> getByCategoryId(Long categoryId) {
        return subCategoryDao.findByCategoryIdAndActiveTrue(categoryId).stream().map(this::toResponse).toList();
    }

    @Override
    public SubCategoryResponseDto update(Long id, SubCategoryRequestDto request) {
        SubCategory subCategory = findActive(id);
        Category category = findActiveCategory(request.categoryId());
        subCategory.setName(request.name());
        subCategory.setDescription(request.description());
        subCategory.setCategory(category);
        return toResponse(subCategoryDao.save(subCategory));
    }

    @Override
    public void delete(Long id) {
        SubCategory subCategory = findActive(id);
        subCategory.setActive(false);
        subCategoryDao.save(subCategory);
    }

    private SubCategory findActive(Long id) {
        return subCategoryDao.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("SubCategory not found: " + id, HttpStatus.NOT_FOUND));
    }

    private Category findActiveCategory(Long categoryId) {
        return categoryDao.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ApplicationException("Category not found: " + categoryId, HttpStatus.NOT_FOUND));
    }

    private SubCategoryResponseDto toResponse(SubCategory sc) {
        return new SubCategoryResponseDto(sc.getId(), sc.getName(), sc.getDescription(),
                sc.getCategory().getId(), sc.getCategory().getName(),
                sc.isActive(), sc.getCreatedAt(), sc.getUpdatedAt());
    }
}
