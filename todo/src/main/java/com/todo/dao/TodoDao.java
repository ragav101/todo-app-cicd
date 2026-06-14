package com.todo.dao;

import com.todo.entity.Todo;
import com.todo.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoDao extends JpaRepository<Todo, Long> {
    List<Todo> findByStatus(TodoStatus status);
    List<Todo> findByCategoryId(Long categoryId);
    List<Todo> findBySubCategoryId(Long subCategoryId);
}
