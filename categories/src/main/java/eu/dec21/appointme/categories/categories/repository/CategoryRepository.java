package eu.dec21.appointme.categories.categories.repository;


import eu.dec21.appointme.categories.categories.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Admin methods - see all categories (active and inactive)
    Page<Category> findByParentId(Long parentId, Pageable pageable);
    Page<Category> findByParentIsNull(Pageable pageable);
    java.util.List<Category> findByParentId(Long parentId);
    
    // Public methods - see only active categories
    Page<Category> findByParentIdAndActiveTrue(Long parentId, Pageable pageable);
    Page<Category> findByParentIsNullAndActiveTrue(Pageable pageable);
    java.util.List<Category> findByParentIdAndActiveTrue(Long parentId);
    
    // Check for duplicate names
    boolean existsByName(String name);
    Optional<Category> findByName(String name);
}
