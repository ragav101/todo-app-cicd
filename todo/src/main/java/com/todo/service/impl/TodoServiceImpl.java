package com.todo.service.impl;

import com.todo.dao.TodoDao;
import com.todo.dto.TodoRequestDto;
import com.todo.dto.TodoResponseDto;
import com.todo.entity.Todo;
import com.todo.entity.TodoStatus;
import com.todo.exception.ApplicationException;
import com.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoDao todoDao;

    public TodoServiceImpl(TodoDao todoDao) {
        this.todoDao = todoDao;
    }

    @Override
    public TodoResponseDto create(TodoRequestDto request) {
        Todo todo = Todo.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .priority(request.priority())
                .categoryId(request.categoryId())
                .subCategoryId(request.subCategoryId())
                .build();
        return toResponse(todoDao.save(todo));
    }

    @Override
    public TodoResponseDto getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public List<TodoResponseDto> getAll() {
        return todoDao.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<TodoResponseDto> getByStatus(TodoStatus status) {
        return todoDao.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Override
    public TodoResponseDto update(Long id, TodoRequestDto request) {
        Todo todo = findById(id);
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setDueDate(request.dueDate());
        todo.setPriority(request.priority());
        todo.setCategoryId(request.categoryId());
        todo.setSubCategoryId(request.subCategoryId());
        return toResponse(todoDao.save(todo));
    }

    @Override
    public TodoResponseDto updateStatus(Long id, TodoStatus status) {
        Todo todo = findById(id);
        todo.setStatus(status);
        return toResponse(todoDao.save(todo));
    }

    @Override
    public void delete(Long id) {
        if (!todoDao.existsById(id)) {
            throw new ApplicationException("Todo not found: " + id, HttpStatus.NOT_FOUND);
        }
        todoDao.deleteById(id);
    }

    private Todo findById(Long id) {
        return todoDao.findById(id)
                .orElseThrow(() -> new ApplicationException("Todo not found: " + id, HttpStatus.NOT_FOUND));
    }

    private TodoResponseDto toResponse(Todo t) {
        return new TodoResponseDto(t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(),
                t.getStatus(), t.getPriority(), t.getCategoryId(),
                t.getSubCategoryId(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
