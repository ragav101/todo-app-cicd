package com.todo.dto;

import com.todo.entity.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating todo status")
public record TodoStatusUpdateDto(

        @Schema(description = "New status", example = "IN_PROGRESS",
                allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
        TodoStatus status
) {}
