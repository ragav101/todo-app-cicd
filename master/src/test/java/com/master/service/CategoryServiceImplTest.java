package com.master.service;

import com.master.dao.CategoryDao;
import com.master.dto.CategoryRequestDto;
import com.master.dto.CategoryResponseDto;
import com.master.entity.Category;
import com.master.exception.ApplicationException;
import com.master.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl — Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryDao categoryDao;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static final Long   ID   = 1L;
    private static final String NAME = "Work";
    private static final String DESC = "Work-related tasks";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private Category buildCategory(Long id, String name, boolean active) {
        Category c = Category.builder().name(name).description(DESC).build();
        c.setId(id);
        c.setActive(active);
        c.setCreatedAt(NOW);
        c.setUpdatedAt(NOW);
        return c;
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create — should persist and return DTO when name is unique")
    void create_whenNameIsUnique_returnsSavedCategory() {
        CategoryRequestDto request = new CategoryRequestDto(NAME, DESC);
        Category saved = buildCategory(ID, NAME, true);

        given(categoryDao.existsByNameIgnoreCase(NAME)).willReturn(false);
        given(categoryDao.save(any(Category.class))).willReturn(saved);

        CategoryResponseDto result = categoryService.create(request);

        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.name()).isEqualTo(NAME);
        assertThat(result.description()).isEqualTo(DESC);
        assertThat(result.active()).isTrue();
        then(categoryDao).should().save(any(Category.class));
    }

    @Test
    @DisplayName("create — should throw CONFLICT when name already exists")
    void create_whenNameAlreadyExists_throwsConflict() {
        CategoryRequestDto request = new CategoryRequestDto(NAME, DESC);
        given(categoryDao.existsByNameIgnoreCase(NAME)).willReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(NAME)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT.value()));

        then(categoryDao).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getById — should return DTO when active category exists")
    void getById_whenActiveExists_returnsCategory() {
        given(categoryDao.findByIdAndActiveTrue(ID)).willReturn(Optional.of(buildCategory(ID, NAME, true)));

        CategoryResponseDto result = categoryService.getById(ID);

        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.name()).isEqualTo(NAME);
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("getById — should throw NOT_FOUND when category is absent or inactive")
    void getById_whenNotFound_throwsNotFoundException() {
        given(categoryDao.findByIdAndActiveTrue(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(ID))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // getAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAll — should return only active categories")
    void getAll_returnsAllActiveCategories() {
        List<Category> categories = List.of(
                buildCategory(1L, "Work",     true),
                buildCategory(2L, "Personal", true)
        );
        given(categoryDao.findByActiveTrue()).willReturn(categories);

        List<CategoryResponseDto> result = categoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategoryResponseDto::name)
                .containsExactly("Work", "Personal");
    }

    @Test
    @DisplayName("getAll — should return empty list when no active categories exist")
    void getAll_whenNoneActive_returnsEmptyList() {
        given(categoryDao.findByActiveTrue()).willReturn(List.of());

        assertThat(categoryService.getAll()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update — should apply all field changes and return updated DTO")
    void update_whenCategoryExists_returnsUpdatedCategory() {
        Category existing = buildCategory(ID, NAME, true);
        CategoryRequestDto request = new CategoryRequestDto("Work Updated", "New description");
        Category updated = buildCategory(ID, "Work Updated", true);
        updated.setDescription("New description");

        given(categoryDao.findByIdAndActiveTrue(ID)).willReturn(Optional.of(existing));
        given(categoryDao.save(existing)).willReturn(updated);

        CategoryResponseDto result = categoryService.update(ID, request);

        assertThat(result.name()).isEqualTo("Work Updated");
        assertThat(result.description()).isEqualTo("New description");
    }

    @Test
    @DisplayName("update — should throw NOT_FOUND when category does not exist")
    void update_whenNotFound_throwsNotFoundException() {
        given(categoryDao.findByIdAndActiveTrue(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(ID, new CategoryRequestDto("X", "Y")))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // delete (soft delete)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete — should set active=false and persist (soft delete)")
    void delete_whenCategoryExists_marksInactiveAndSaves() {
        Category category = buildCategory(ID, NAME, true);
        given(categoryDao.findByIdAndActiveTrue(ID)).willReturn(Optional.of(category));

        categoryService.delete(ID);

        assertThat(category.isActive()).isFalse();
        then(categoryDao).should().save(category);
    }

    @Test
    @DisplayName("delete — should throw NOT_FOUND when category does not exist")
    void delete_whenNotFound_throwsNotFoundException() {
        given(categoryDao.findByIdAndActiveTrue(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(ID))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }
}
