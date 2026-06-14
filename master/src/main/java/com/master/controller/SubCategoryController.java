package com.master.controller;

import com.master.dto.SubCategoryRequestDto;
import com.master.dto.SubCategoryResponseDto;
import com.master.service.SubCategoryService;
import com.master.utility.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SubCategory", description = "SubCategory management APIs")
@RestController
@RequestMapping("/api/subcategories")
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    public SubCategoryController(SubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }

    @Operation(summary = "Create a new subcategory")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "SubCategory created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parent category not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "SubCategory name already exists in this category")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SubCategoryResponseDto>> create(@Valid @RequestBody SubCategoryRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SubCategory created successfully", subCategoryService.create(request)));
    }

    @Operation(summary = "Get all active subcategories")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubCategoryResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(subCategoryService.getAll()));
    }

    @Operation(summary = "Get a subcategory by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubCategoryResponseDto>> getById(
            @Parameter(description = "SubCategory ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(subCategoryService.getById(id)));
    }

    @Operation(summary = "Get all subcategories under a category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<SubCategoryResponseDto>>> getByCategoryId(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(subCategoryService.getByCategoryId(categoryId)));
    }

    @Operation(summary = "Update a subcategory")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubCategoryResponseDto>> update(
            @Parameter(description = "SubCategory ID") @PathVariable Long id,
            @Valid @RequestBody SubCategoryRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("SubCategory updated successfully", subCategoryService.update(id, request)));
    }

    @Operation(summary = "Soft-delete a subcategory")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "SubCategory ID") @PathVariable Long id) {
        subCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SubCategory deleted successfully", null));
    }
}
