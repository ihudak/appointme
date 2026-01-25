package eu.dec21.appointme.categories.categories.repository;


import eu.dec21.appointme.categories.categories.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
