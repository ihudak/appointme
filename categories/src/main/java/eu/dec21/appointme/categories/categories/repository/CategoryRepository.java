package eu.dec21.appointme.categories.categories.repository;


import eu.dec21.appointme.categories.categories.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByParentId(Long parentId, Pageable pageable);
    Page<Category> findByParentIsNull(Pageable pageable);
}
