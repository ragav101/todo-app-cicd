package com.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating a category")
public record CategoryRequestDto(

        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
        @Schema(description = "Unique category name", example = "Work")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Optional description", example = "Work related tasks and projects")
        String description
) {}
