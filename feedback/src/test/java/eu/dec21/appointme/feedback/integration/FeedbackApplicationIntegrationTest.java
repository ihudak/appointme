package eu.dec21.appointme.feedback.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Feedback module using Testcontainers.
 * Validates that the Spring Boot application starts correctly with a real database.
 * This is a placeholder test until the Feedback module is fully implemented.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class FeedbackApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("appme_feedback")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Test
    void shouldConnectToPostgresContainer() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldStartSpringBootApplication() {
        // This test validates that the Spring Boot application context loads successfully
        // with Testcontainers providing the database connection
        assertThat(postgres.isRunning()).isTrue();
    }

    // TODO: Add tests for Feedback entity CRUD operations when the module is implemented
    // Expected tests:
    // - shouldCreateAndRetrieveFeedback
    // - shouldFindFeedbackByBusinessId
    // - shouldFindFeedbackByUserId
    // - shouldUpdateFeedbackRating
    // - shouldDeleteFeedback
}
