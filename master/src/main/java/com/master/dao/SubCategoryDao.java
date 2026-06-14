package com.master.dao;

import com.master.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryDao extends JpaRepository<SubCategory, Long> {
    List<SubCategory> findByActiveTrue();
    List<SubCategory> findByCategoryIdAndActiveTrue(Long categoryId);
    Optional<SubCategory> findByIdAndActiveTrue(Long id);
    boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);
}
