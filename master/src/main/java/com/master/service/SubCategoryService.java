package com.master.service;

import com.master.dto.SubCategoryRequestDto;
import com.master.dto.SubCategoryResponseDto;

import java.util.List;

public interface SubCategoryService {
    SubCategoryResponseDto create(SubCategoryRequestDto request);
    SubCategoryResponseDto getById(Long id);
    List<SubCategoryResponseDto> getAll();
    List<SubCategoryResponseDto> getByCategoryId(Long categoryId);
    SubCategoryResponseDto update(Long id, SubCategoryRequestDto request);
    void delete(Long id);
}
