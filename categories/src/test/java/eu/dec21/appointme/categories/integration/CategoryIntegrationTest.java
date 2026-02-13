package eu.dec21.appointme.categories.integration;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Category module using Testcontainers.
 */
@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
class CategoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("appme_categories")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private CategoryRepository categoryRepository;

    private void setAuditFields(Category category) {
        category.setCreatedBy(999L);
        category.setUpdatedBy(999L);
    }

    @Test
    void shouldConnectToPostgresContainer() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldCreateAndRetrieveCategory() {
        Category category = Category.builder()
                .name("Test Category")
                .description("A test category")
                .active(true)
                .build();
        setAuditFields(category);

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Test Category");
        assertThat(savedCategory.getCreatedBy()).isEqualTo(999L);
    }

    @Test
    void shouldCreateCategoryHierarchy() {
        Category parent = Category.builder()
                .name("Parent Category")
                .description("A parent")
                .active(true)
                .build();
        setAuditFields(parent);
        Category savedParent = categoryRepository.save(parent);

        Category child = Category.builder()
                .name("Child Category")
                .description("A child")
                .active(true)
                .parent(savedParent)
                .build();
        setAuditFields(child);
        Category savedChild = categoryRepository.save(child);

        assertThat(savedChild.getParent().getId()).isEqualTo(savedParent.getId());
    }

    @Test
    void shouldFindActiveCategoriesOnly() {
        Category active = Category.builder()
                .name("Active Cat")
                .active(true)
                .build();
        setAuditFields(active);

        Category inactive = Category.builder()
                .name("Inactive Cat")
                .active(false)
                .build();
        setAuditFields(inactive);

        categoryRepository.save(active);
        categoryRepository.save(inactive);

        var allCategories = categoryRepository.findAll();
        var activeOnly = allCategories.stream().filter(Category::isActive).toList();

        assertThat(activeOnly).hasSize(1);
        assertThat(activeOnly.get(0).getName()).isEqualTo("Active Cat");
    }
}
