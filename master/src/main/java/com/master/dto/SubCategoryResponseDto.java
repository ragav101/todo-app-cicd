package com.master.dto;

import java.time.LocalDateTime;

public record SubCategoryResponseDto(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
