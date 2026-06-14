package com.todo.service;

import com.todo.dto.TodoRequestDto;
import com.todo.dto.TodoResponseDto;
import com.todo.entity.TodoStatus;

import java.util.List;

public interface TodoService {
    TodoResponseDto create(TodoRequestDto request);
    TodoResponseDto getById(Long id);
    List<TodoResponseDto> getAll();
    List<TodoResponseDto> getByStatus(TodoStatus status);
    TodoResponseDto update(Long id, TodoRequestDto request);
    TodoResponseDto updateStatus(Long id, TodoStatus status);
    void delete(Long id);
}
