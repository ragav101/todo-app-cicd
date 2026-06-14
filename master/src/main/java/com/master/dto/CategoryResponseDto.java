package com.master.dto;

import java.time.LocalDateTime;

public record CategoryResponseDto(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
