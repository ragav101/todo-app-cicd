package com.master.dao;

import com.master.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryDao extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrue();
    Optional<Category> findByIdAndActiveTrue(Long id);
    boolean existsByNameIgnoreCase(String name);
}
