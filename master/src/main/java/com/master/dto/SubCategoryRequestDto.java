package com.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating or updating a subcategory")
public record SubCategoryRequestDto(

        @Schema(description = "Unique subcategory name within the parent category", example = "Meetings")
        String name,

        @Schema(description = "Optional description", example = "Scheduled meetings and calls")
        String description,

        @Schema(description = "ID of the parent category", example = "1")
        Long categoryId
) {}
