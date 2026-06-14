package com.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating or updating a category")
public record CategoryRequestDto(

        @Schema(description = "Unique category name", example = "Work")
        String name,

        @Schema(description = "Optional description", example = "Work related tasks and projects")
        String description
) {}
