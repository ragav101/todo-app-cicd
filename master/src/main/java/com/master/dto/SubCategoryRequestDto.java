package com.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating a subcategory")
public record SubCategoryRequestDto(

        @NotBlank(message = "SubCategory name is required")
        @Size(min = 2, max = 100, message = "SubCategory name must be between 2 and 100 characters")
        @Schema(description = "Unique subcategory name within the parent category", example = "Meetings")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Optional description", example = "Scheduled meetings and calls")
        String description,

        @NotNull(message = "Category ID is required")
        @Schema(description = "ID of the parent category", example = "1")
        Long categoryId
) {}
