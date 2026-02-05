package eu.dec21.appointme.categories.integration;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Category module using Testcontainers.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CategoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:16-3.4")
            .withDatabaseName("appme_categories")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateAndRetrieveCategory() {
        Category category = Category.builder()
                .name("Test Category")
                .description("A test category")
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Test Category");
    }
}
