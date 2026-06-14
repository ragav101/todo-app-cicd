package com.master.service;

import com.master.dao.CategoryDao;
import com.master.dao.SubCategoryDao;
import com.master.dto.SubCategoryRequestDto;
import com.master.dto.SubCategoryResponseDto;
import com.master.entity.Category;
import com.master.entity.SubCategory;
import com.master.exception.ApplicationException;
import com.master.service.impl.SubCategoryServiceImpl;
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
@DisplayName("SubCategoryServiceImpl — Unit Tests")
class SubCategoryServiceImplTest {

    @Mock
    private SubCategoryDao subCategoryDao;

    @Mock
    private CategoryDao categoryDao;

    @InjectMocks
    private SubCategoryServiceImpl subCategoryService;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static final Long   CAT_ID  = 1L;
    private static final Long   SC_ID   = 10L;
    private static final String CAT_NAME = "Work";
    private static final String SC_NAME  = "Meetings";
    private static final String SC_DESC  = "Scheduled meetings and calls";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private Category buildCategory(Long id, String name) {
        Category c = Category.builder().name(name).build();
        c.setId(id);
        c.setActive(true);
        c.setCreatedAt(NOW);
        c.setUpdatedAt(NOW);
        return c;
    }

    private SubCategory buildSubCategory(Long id, String name, Category category) {
        SubCategory sc = SubCategory.builder()
                .name(name)
                .description(SC_DESC)
                .category(category)
                .build();
        sc.setId(id);
        sc.setActive(true);
        sc.setCreatedAt(NOW);
        sc.setUpdatedAt(NOW);
        return sc;
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create — should persist and return DTO when request is valid")
    void create_whenValidRequest_returnsSavedSubCategory() {
        Category category  = buildCategory(CAT_ID, CAT_NAME);
        SubCategory saved  = buildSubCategory(SC_ID, SC_NAME, category);
        SubCategoryRequestDto request = new SubCategoryRequestDto(SC_NAME, SC_DESC, CAT_ID);

        given(categoryDao.findByIdAndActiveTrue(CAT_ID)).willReturn(Optional.of(category));
        given(subCategoryDao.existsByNameIgnoreCaseAndCategoryId(SC_NAME, CAT_ID)).willReturn(false);
        given(subCategoryDao.save(any(SubCategory.class))).willReturn(saved);

        SubCategoryResponseDto result = subCategoryService.create(request);

        assertThat(result.id()).isEqualTo(SC_ID);
        assertThat(result.name()).isEqualTo(SC_NAME);
        assertThat(result.categoryId()).isEqualTo(CAT_ID);
        assertThat(result.categoryName()).isEqualTo(CAT_NAME);
        assertThat(result.active()).isTrue();
        then(subCategoryDao).should().save(any(SubCategory.class));
    }

    @Test
    @DisplayName("create — should throw NOT_FOUND when parent category does not exist")
    void create_whenParentCategoryNotFound_throwsNotFoundException() {
        SubCategoryRequestDto request = new SubCategoryRequestDto(SC_NAME, SC_DESC, CAT_ID);
        given(categoryDao.findByIdAndActiveTrue(CAT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subCategoryService.create(request))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));

        then(subCategoryDao).should(never()).save(any());
    }

    @Test
    @DisplayName("create — should throw CONFLICT when name already exists within the same category")
    void create_whenNameExistsInCategory_throwsConflict() {
        Category category = buildCategory(CAT_ID, CAT_NAME);
        SubCategoryRequestDto request = new SubCategoryRequestDto(SC_NAME, SC_DESC, CAT_ID);

        given(categoryDao.findByIdAndActiveTrue(CAT_ID)).willReturn(Optional.of(category));
        given(subCategoryDao.existsByNameIgnoreCaseAndCategoryId(SC_NAME, CAT_ID)).willReturn(true);

        assertThatThrownBy(() -> subCategoryService.create(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(SC_NAME)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT.value()));

        then(subCategoryDao).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getById — should return DTO when active subcategory exists")
    void getById_whenActiveExists_returnsSubCategory() {
        Category cat = buildCategory(CAT_ID, CAT_NAME);
        SubCategory sc = buildSubCategory(SC_ID, SC_NAME, cat);
        given(subCategoryDao.findByIdAndActiveTrue(SC_ID)).willReturn(Optional.of(sc));

        SubCategoryResponseDto result = subCategoryService.getById(SC_ID);

        assertThat(result.id()).isEqualTo(SC_ID);
        assertThat(result.name()).isEqualTo(SC_NAME);
        assertThat(result.categoryId()).isEqualTo(CAT_ID);
    }

    @Test
    @DisplayName("getById — should throw NOT_FOUND when subcategory is absent or inactive")
    void getById_whenNotFound_throwsNotFoundException() {
        given(subCategoryDao.findByIdAndActiveTrue(SC_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subCategoryService.getById(SC_ID))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // getAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAll — should return all active subcategories")
    void getAll_returnsAllActiveSubCategories() {
        Category cat = buildCategory(CAT_ID, CAT_NAME);
        List<SubCategory> subCategories = List.of(
                buildSubCategory(10L, "Meetings", cat),
                buildSubCategory(11L, "Emails",   cat)
        );
        given(subCategoryDao.findByActiveTrue()).willReturn(subCategories);

        List<SubCategoryResponseDto> result = subCategoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SubCategoryResponseDto::name)
                .containsExactly("Meetings", "Emails");
    }

    @Test
    @DisplayName("getAll — should return empty list when no active subcategories exist")
    void getAll_whenNoneActive_returnsEmptyList() {
        given(subCategoryDao.findByActiveTrue()).willReturn(List.of());

        assertThat(subCategoryService.getAll()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getByCategoryId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getByCategoryId — should return subcategories filtered by parent category")
    void getByCategoryId_returnsSubCategoriesForGivenCategory() {
        Category cat = buildCategory(CAT_ID, CAT_NAME);
        List<SubCategory> subCategories = List.of(buildSubCategory(SC_ID, SC_NAME, cat));
        given(subCategoryDao.findByCategoryIdAndActiveTrue(CAT_ID)).willReturn(subCategories);

        List<SubCategoryResponseDto> result = subCategoryService.getByCategoryId(CAT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoryId()).isEqualTo(CAT_ID);
    }

    @Test
    @DisplayName("getByCategoryId — should return empty list when category has no active subcategories")
    void getByCategoryId_whenNone_returnsEmptyList() {
        given(subCategoryDao.findByCategoryIdAndActiveTrue(CAT_ID)).willReturn(List.of());

        assertThat(subCategoryService.getByCategoryId(CAT_ID)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update — should apply all field changes and return updated DTO")
    void update_whenSubCategoryExists_returnsUpdated() {
        Category cat = buildCategory(CAT_ID, CAT_NAME);
        SubCategory existing = buildSubCategory(SC_ID, SC_NAME, cat);
        SubCategoryRequestDto request = new SubCategoryRequestDto("Calls", "Phone calls", CAT_ID);
        SubCategory updated = buildSubCategory(SC_ID, "Calls", cat);
        updated.setDescription("Phone calls");

        given(subCategoryDao.findByIdAndActiveTrue(SC_ID)).willReturn(Optional.of(existing));
        given(categoryDao.findByIdAndActiveTrue(CAT_ID)).willReturn(Optional.of(cat));
        given(subCategoryDao.save(existing)).willReturn(updated);

        SubCategoryResponseDto result = subCategoryService.update(SC_ID, request);

        assertThat(result.name()).isEqualTo("Calls");
        assertThat(result.description()).isEqualTo("Phone calls");
    }

    @Test
    @DisplayName("update — should throw NOT_FOUND when subcategory does not exist")
    void update_whenNotFound_throwsNotFoundException() {
        given(subCategoryDao.findByIdAndActiveTrue(SC_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subCategoryService.update(SC_ID, new SubCategoryRequestDto("X", "Y", CAT_ID)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // delete (soft delete)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete — should set active=false and persist (soft delete)")
    void delete_whenSubCategoryExists_marksInactiveAndSaves() {
        Category cat = buildCategory(CAT_ID, CAT_NAME);
        SubCategory sc = buildSubCategory(SC_ID, SC_NAME, cat);
        given(subCategoryDao.findByIdAndActiveTrue(SC_ID)).willReturn(Optional.of(sc));

        subCategoryService.delete(SC_ID);

        assertThat(sc.isActive()).isFalse();
        then(subCategoryDao).should().save(sc);
    }

    @Test
    @DisplayName("delete — should throw NOT_FOUND when subcategory does not exist")
    void delete_whenNotFound_throwsNotFoundException() {
        given(subCategoryDao.findByIdAndActiveTrue(SC_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subCategoryService.delete(SC_ID))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }
}
