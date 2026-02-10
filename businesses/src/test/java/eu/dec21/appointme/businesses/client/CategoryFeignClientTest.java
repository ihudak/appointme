package eu.dec21.appointme.businesses.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit tests for CategoryFeignClient using WireMock.
 * Tests the Feign client integration with the Categories microservice.
 */
@SpringBootTest
@WireMockTest(httpPort = 8089)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "categories.service.url=http://localhost:8089",
        "feign.client.config.categories.connectTimeout=2000",
        "feign.client.config.categories.readTimeout=2000"
})
@DisplayName("CategoryFeignClient Tests")
class CategoryFeignClientTest {

    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    @DisplayName("Should successfully retrieve subcategory IDs for a valid category")
    void testGetAllSubcategoryIds_Success() {
        // Given
        Long categoryId = 100L;
        String responseBody = "[200, 201, 202, 203]";

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // When
        Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result).containsExactlyInAnyOrder(200L, 201L, 202L, 203L);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should return empty set when category has no subcategories")
    void testGetAllSubcategoryIds_EmptyResult() {
        // Given
        Long categoryId = 500L;
        String responseBody = "[]";

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // When
        Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle large number of subcategories")
    void testGetAllSubcategoryIds_LargeSet() {
        // Given
        Long categoryId = 999L;
        StringBuilder responseBody = new StringBuilder("[");
        for (int i = 1; i <= 100; i++) {
            responseBody.append(i);
            if (i < 100) {
                responseBody.append(",");
            }
        }
        responseBody.append("]");

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody.toString())));

        // When
        Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(100);
        assertThat(result).contains(1L, 50L, 100L);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle single subcategory")
    void testGetAllSubcategoryIds_SingleSubcategory() {
        // Given
        Long categoryId = 300L;
        String responseBody = "[301]";

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // When
        Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(301L);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should throw FeignException when category not found (404)")
    void testGetAllSubcategoryIds_CategoryNotFound() {
        // Given
        Long categoryId = 999L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Category not found\"}")));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.NotFound.class)
                .hasMessageContaining("404");

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should throw FeignException when server error occurs (500)")
    void testGetAllSubcategoryIds_ServerError() {
        // Given
        Long categoryId = 100L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal server error\"}")));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.InternalServerError.class)
                .hasMessageContaining("500");

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should throw FeignException when bad request (400)")
    void testGetAllSubcategoryIds_BadRequest() {
        // Given
        Long categoryId = 100L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Invalid category ID\"}")));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.BadRequest.class)
                .hasMessageContaining("400");

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should throw FeignException when service unavailable (503)")
    void testGetAllSubcategoryIds_ServiceUnavailable() {
        // Given
        Long categoryId = 100L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service temporarily unavailable\"}")));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.ServiceUnavailable.class)
                .hasMessageContaining("503");

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle malformed JSON response gracefully")
    void testGetAllSubcategoryIds_MalformedJson() {
        // Given
        Long categoryId = 100L;
        String malformedJson = "[200, 201, invalid]";

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(malformedJson)));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.class);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle connection timeout")
    void testGetAllSubcategoryIds_ConnectionTimeout() {
        // Given
        Long categoryId = 100L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withFixedDelay(3000))); // Delay longer than client timeout

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.class);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should correctly construct URL with different category IDs")
    void testGetAllSubcategoryIds_DifferentCategoryIds() {
        // Test with various category IDs
        Long[] categoryIds = {1L, 999L, 12345L};

        for (Long categoryId : categoryIds) {
            String responseBody = "[" + (categoryId + 1) + "]";

            stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseBody)));

            // When
            Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).containsExactly(categoryId + 1);

            verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
        }
    }

    @Test
    @DisplayName("Should handle duplicate IDs in response (Set should deduplicate)")
    void testGetAllSubcategoryIds_DuplicateIds() {
        // Given
        Long categoryId = 100L;
        String responseBody = "[200, 201, 200, 202, 201, 203]"; // Contains duplicates

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // When
        Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4); // Should be deduplicated
        assertThat(result).containsExactlyInAnyOrder(200L, 201L, 202L, 203L);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle response with very large IDs")
    void testGetAllSubcategoryIds_LargeIds() {
        // Given
        Long categoryId = 100L;
        String responseBody = "[9223372036854775806, 9223372036854775807]"; // Near Long.MAX_VALUE

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // When
        Set<Long> result = categoryFeignClient.getAllSubcategoryIds(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(9223372036854775806L, 9223372036854775807L);

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should retry on transient errors if configured")
    void testGetAllSubcategoryIds_RetryBehavior() {
        // Given
        Long categoryId = 100L;
        String responseBody = "[200, 201]";

        // First call fails, second succeeds
        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(500))
                .willSetStateTo("First Attempt Failed"));

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Attempt Failed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // When/Then - depending on retry configuration
        // This test documents the retry behavior
        // If no retry is configured, it will throw on first attempt
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.InternalServerError.class);

        verify(1, getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle unauthorized access (401)")
    void testGetAllSubcategoryIds_Unauthorized() {
        // Given
        Long categoryId = 100L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Unauthorized access\"}")));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.Unauthorized.class)
                .hasMessageContaining("401");

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }

    @Test
    @DisplayName("Should handle forbidden access (403)")
    void testGetAllSubcategoryIds_Forbidden() {
        // Given
        Long categoryId = 100L;

        stubFor(get(urlEqualTo("/categories/" + categoryId + "/subcategories/ids"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Access forbidden\"}")));

        // When/Then
        assertThatThrownBy(() -> categoryFeignClient.getAllSubcategoryIds(categoryId))
                .isInstanceOf(FeignException.Forbidden.class)
                .hasMessageContaining("403");

        verify(getRequestedFor(urlEqualTo("/categories/" + categoryId + "/subcategories/ids")));
    }
}
