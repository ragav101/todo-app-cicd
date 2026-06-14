package com.todo.service;

import com.todo.dao.TodoDao;
import com.todo.dto.TodoRequestDto;
import com.todo.dto.TodoResponseDto;
import com.todo.entity.Todo;
import com.todo.entity.TodoPriority;
import com.todo.entity.TodoStatus;
import com.todo.exception.ApplicationException;
import com.todo.service.impl.TodoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
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
@DisplayName("TodoServiceImpl — Unit Tests")
class TodoServiceImplTest {

    @Mock
    private TodoDao todoDao;

    @InjectMocks
    private TodoServiceImpl todoService;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static final Long         ID          = 1L;
    private static final String       TITLE       = "Prepare Q3 Report";
    private static final String       DESCRIPTION = "Compile sales data and create slides";
    private static final LocalDate    DUE_DATE    = LocalDate.of(2026, 9, 30);
    private static final Long         CATEGORY_ID = 1L;
    private static final Long         SUB_CAT_ID  = 2L;
    private static final LocalDateTime NOW        = LocalDateTime.now();

    private Todo buildTodo(Long id, String title, TodoStatus status, TodoPriority priority) {
        Todo t = Todo.builder()
                .title(title)
                .description(DESCRIPTION)
                .dueDate(DUE_DATE)
                .status(status)
                .priority(priority)
                .categoryId(CATEGORY_ID)
                .subCategoryId(SUB_CAT_ID)
                .build();
        t.setId(id);
        t.setCreatedAt(NOW);
        t.setUpdatedAt(NOW);
        return t;
    }

    private TodoRequestDto buildRequest(String title, TodoPriority priority) {
        return new TodoRequestDto(title, DESCRIPTION, DUE_DATE, priority, null, CATEGORY_ID, SUB_CAT_ID);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create — should persist and return DTO with PENDING status and MEDIUM priority by default")
    void create_validRequest_returnsSavedTodoWithDefaults() {
        TodoRequestDto request = buildRequest(TITLE, null);
        Todo saved = buildTodo(ID, TITLE, TodoStatus.PENDING, TodoPriority.MEDIUM);

        given(todoDao.save(any(Todo.class))).willReturn(saved);

        TodoResponseDto result = todoService.create(request);

        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.title()).isEqualTo(TITLE);
        assertThat(result.status()).isEqualTo(TodoStatus.PENDING);
        assertThat(result.priority()).isEqualTo(TodoPriority.MEDIUM);
        assertThat(result.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(result.subCategoryId()).isEqualTo(SUB_CAT_ID);
        then(todoDao).should().save(any(Todo.class));
    }

    @Test
    @DisplayName("create — should persist and return DTO with explicit priority when provided")
    void create_withExplicitPriority_returnsSavedTodoWithThatPriority() {
        TodoRequestDto request = buildRequest(TITLE, TodoPriority.HIGH);
        Todo saved = buildTodo(ID, TITLE, TodoStatus.PENDING, TodoPriority.HIGH);

        given(todoDao.save(any(Todo.class))).willReturn(saved);

        TodoResponseDto result = todoService.create(request);

        assertThat(result.priority()).isEqualTo(TodoPriority.HIGH);
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getById — should return DTO when todo exists")
    void getById_whenExists_returnsTodo() {
        Todo todo = buildTodo(ID, TITLE, TodoStatus.PENDING, TodoPriority.MEDIUM);
        given(todoDao.findById(ID)).willReturn(Optional.of(todo));

        TodoResponseDto result = todoService.getById(ID);

        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.title()).isEqualTo(TITLE);
    }

    @Test
    @DisplayName("getById — should throw NOT_FOUND when todo does not exist")
    void getById_whenNotFound_throwsNotFoundException() {
        given(todoDao.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.getById(ID))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // getAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAll — should return all todos")
    void getAll_returnsAllTodos() {
        List<Todo> todos = List.of(
                buildTodo(1L, "Task A", TodoStatus.PENDING,     TodoPriority.LOW),
                buildTodo(2L, "Task B", TodoStatus.IN_PROGRESS, TodoPriority.HIGH)
        );
        given(todoDao.findAll()).willReturn(todos);

        List<TodoResponseDto> result = todoService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TodoResponseDto::title)
                .containsExactly("Task A", "Task B");
    }

    @Test
    @DisplayName("getAll — should return empty list when no todos exist")
    void getAll_whenNone_returnsEmptyList() {
        given(todoDao.findAll()).willReturn(List.of());

        assertThat(todoService.getAll()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getByStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getByStatus — should return only todos matching the given status")
    void getByStatus_returnsTodosFilteredByStatus() {
        List<Todo> pending = List.of(
                buildTodo(1L, "Task A", TodoStatus.PENDING, TodoPriority.MEDIUM),
                buildTodo(2L, "Task B", TodoStatus.PENDING, TodoPriority.LOW)
        );
        given(todoDao.findByStatus(TodoStatus.PENDING)).willReturn(pending);

        List<TodoResponseDto> result = todoService.getByStatus(TodoStatus.PENDING);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.status() == TodoStatus.PENDING);
    }

    @Test
    @DisplayName("getByStatus — should return empty list when no todos match the given status")
    void getByStatus_whenNoneMatch_returnsEmptyList() {
        given(todoDao.findByStatus(TodoStatus.CANCELLED)).willReturn(List.of());

        assertThat(todoService.getByStatus(TodoStatus.CANCELLED)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update — should apply all field changes and return updated DTO")
    void update_whenTodoExists_returnsUpdated() {
        Todo existing = buildTodo(ID, TITLE, TodoStatus.PENDING, TodoPriority.MEDIUM);
        TodoRequestDto request = buildRequest("Updated Title", TodoPriority.URGENT);
        Todo updated = buildTodo(ID, "Updated Title", TodoStatus.PENDING, TodoPriority.URGENT);

        given(todoDao.findById(ID)).willReturn(Optional.of(existing));
        given(todoDao.save(existing)).willReturn(updated);

        TodoResponseDto result = todoService.update(ID, request);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.priority()).isEqualTo(TodoPriority.URGENT);
    }

    @Test
    @DisplayName("update — should throw NOT_FOUND when todo does not exist")
    void update_whenNotFound_throwsNotFoundException() {
        given(todoDao.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.update(ID, buildRequest("X", TodoPriority.LOW)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // updateStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateStatus — should change only the status field and return updated DTO")
    void updateStatus_whenTodoExists_returnsUpdatedStatus() {
        Todo existing = buildTodo(ID, TITLE, TodoStatus.PENDING, TodoPriority.MEDIUM);
        Todo updated  = buildTodo(ID, TITLE, TodoStatus.IN_PROGRESS, TodoPriority.MEDIUM);

        given(todoDao.findById(ID)).willReturn(Optional.of(existing));
        given(todoDao.save(existing)).willReturn(updated);

        TodoResponseDto result = todoService.updateStatus(ID, TodoStatus.IN_PROGRESS);

        assertThat(result.status()).isEqualTo(TodoStatus.IN_PROGRESS);
        assertThat(result.title()).isEqualTo(TITLE);
    }

    @Test
    @DisplayName("updateStatus — should throw NOT_FOUND when todo does not exist")
    void updateStatus_whenNotFound_throwsNotFoundException() {
        given(todoDao.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.updateStatus(ID, TodoStatus.COMPLETED))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete — should remove todo when it exists")
    void delete_whenTodoExists_deletesSuccessfully() {
        given(todoDao.existsById(ID)).willReturn(true);

        todoService.delete(ID);

        then(todoDao).should().deleteById(ID);
    }

    @Test
    @DisplayName("delete — should throw NOT_FOUND when todo does not exist")
    void delete_whenNotFound_throwsNotFoundException() {
        given(todoDao.existsById(ID)).willReturn(false);

        assertThatThrownBy(() -> todoService.delete(ID))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));

        then(todoDao).should(never()).deleteById(any());
    }
}
