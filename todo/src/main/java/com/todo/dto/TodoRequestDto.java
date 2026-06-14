package com.todo.dto;

import com.todo.entity.TodoPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Request body for creating or updating a todo")
public record TodoRequestDto(

        @Schema(description = "Short title of the todo", example = "Prepare Q3 report")
        String title,

        @Schema(description = "Detailed description", example = "Compile sales data and create slides")
        String description,

        @Schema(description = "Due date in ISO format", example = "2026-06-30")
        LocalDate dueDate,

        @Schema(description = "Priority level", example = "HIGH",
                allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
        TodoPriority priority,

        @Schema(description = "ID of the user this todo belongs to", example = "1")
        Long userId,

        @Schema(description = "Category ID from master service", example = "1")
        Long categoryId,

        @Schema(description = "SubCategory ID from master service", example = "1")
        Long subCategoryId
) {}
