package com.todo.dto;

import com.todo.entity.TodoPriority;
import com.todo.entity.TodoStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoResponseDto(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        TodoStatus status,
        TodoPriority priority,
        Long categoryId,
        Long subCategoryId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
