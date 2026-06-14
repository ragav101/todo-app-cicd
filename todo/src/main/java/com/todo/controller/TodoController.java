package com.todo.controller;

import com.todo.dto.TodoRequestDto;
import com.todo.dto.TodoResponseDto;
import com.todo.dto.TodoStatusUpdateDto;
import com.todo.entity.TodoStatus;
import com.todo.service.TodoService;
import com.todo.utility.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Todo", description = "Todo management APIs")
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @Operation(summary = "Create a new todo")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Todo created")
    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponseDto>> create(@RequestBody TodoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Todo created successfully", todoService.create(request)));
    }

    @Operation(summary = "Get all todos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(todoService.getAll()));
    }

    @Operation(summary = "Get a todo by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponseDto>> getById(
            @Parameter(description = "Todo ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(todoService.getById(id)));
    }

    @Operation(summary = "Get todos by status",
               description = "Valid values: PENDING, IN_PROGRESS, COMPLETED, CANCELLED")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TodoResponseDto>>> getByStatus(
            @Parameter(description = "Todo status") @PathVariable TodoStatus status) {
        return ResponseEntity.ok(ApiResponse.success(todoService.getByStatus(status)));
    }

    @Operation(summary = "Update a todo (full update)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponseDto>> update(
            @Parameter(description = "Todo ID") @PathVariable Long id,
            @RequestBody TodoRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Todo updated successfully", todoService.update(id, request)));
    }

    @Operation(summary = "Update todo status only")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TodoResponseDto>> updateStatus(
            @Parameter(description = "Todo ID") @PathVariable Long id,
            @RequestBody TodoStatusUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", todoService.updateStatus(id, request.status())));
    }

    @Operation(summary = "Delete a todo")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Todo ID") @PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Todo deleted successfully", null));
    }
}
