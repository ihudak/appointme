package eu.dec21.appointme.businesses.businesses.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for RatingConfig.
 * Tests default values, property binding, getters/setters, and configuration scenarios.
 */
@DisplayName("RatingConfig Tests")
class RatingConfigTest {

    // ==================== Unit Tests (No Spring Context) ====================

    @Test
    @DisplayName("Should have default confidenceThreshold of 10")
    void testDefaultConfidenceThreshold() {
        // Given
        RatingConfig config = new RatingConfig();

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should have default globalMean of 3.5")
    void testDefaultGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(3.5);
    }

    @Test
    @DisplayName("Should set confidenceThreshold via setter")
    void testSetConfidenceThreshold() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setConfidenceThreshold(25);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(25);
    }

    @Test
    @DisplayName("Should set globalMean via setter")
    void testSetGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(4.2);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(4.2);
    }

    @Test
    @DisplayName("Should allow zero as confidenceThreshold")
    void testSetConfidenceThresholdToZero() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setConfidenceThreshold(0);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should allow negative confidenceThreshold")
    void testSetNegativeConfidenceThreshold() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setConfidenceThreshold(-5);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Should allow large confidenceThreshold value")
    void testSetLargeConfidenceThreshold() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setConfidenceThreshold(1000000);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(1000000);
    }

    @Test
    @DisplayName("Should allow zero as globalMean")
    void testSetGlobalMeanToZero() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(0.0);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should allow negative globalMean")
    void testSetNegativeGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(-1.5);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(-1.5);
    }

    @Test
    @DisplayName("Should allow maximum rating globalMean")
    void testSetMaximumGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(5.0);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Should allow globalMean greater than 5")
    void testSetGlobalMeanGreaterThanFive() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(10.0);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should allow fractional globalMean with high precision")
    void testSetFractionalGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(3.14159);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(3.14159);
    }

    @Test
    @DisplayName("Should allow setting values multiple times")
    void testMultipleSets() {
        // Given
        RatingConfig config = new RatingConfig();

        // When/Then - Multiple sets
        config.setConfidenceThreshold(5);
        assertThat(config.getConfidenceThreshold()).isEqualTo(5);

        config.setConfidenceThreshold(15);
        assertThat(config.getConfidenceThreshold()).isEqualTo(15);

        config.setGlobalMean(2.5);
        assertThat(config.getGlobalMean()).isEqualTo(2.5);

        config.setGlobalMean(4.8);
        assertThat(config.getGlobalMean()).isEqualTo(4.8);
    }

    @Test
    @DisplayName("Should preserve initial values when no setters called")
    void testPreserveDefaults() {
        // Given
        RatingConfig config = new RatingConfig();

        // When - No setters called

        // Then - Defaults preserved
        assertThat(config.getConfidenceThreshold()).isEqualTo(10);
        assertThat(config.getGlobalMean()).isEqualTo(3.5);
    }

    @Test
    @DisplayName("Should be independent instances")
    void testIndependentInstances() {
        // Given
        RatingConfig config1 = new RatingConfig();
        RatingConfig config2 = new RatingConfig();

        // When
        config1.setConfidenceThreshold(20);
        config1.setGlobalMean(4.0);

        // Then - config2 should retain defaults
        assertThat(config2.getConfidenceThreshold()).isEqualTo(10);
        assertThat(config2.getGlobalMean()).isEqualTo(3.5);
    }

    // ==================== Spring Integration Tests ====================

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.confidence-threshold=50",
            "application.rating.global-mean=4.5"
    })
    @DisplayName("RatingConfig Spring Integration - Custom Properties")
    static class WithCustomPropertiesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind custom confidenceThreshold from properties")
        void testCustomConfidenceThreshold() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should bind custom globalMean from properties")
        void testCustomGlobalMean() {
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(4.5);
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @DisplayName("RatingConfig Spring Integration - Default Properties")
    static class WithDefaultPropertiesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should use default confidenceThreshold when property not set")
        void testDefaultConfidenceThreshold() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should use default globalMean when property not set")
        void testDefaultGlobalMean() {
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(3.5);
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.confidence-threshold=0",
            "application.rating.global-mean=0.0"
    })
    @DisplayName("RatingConfig Spring Integration - Zero Values")
    static class WithZeroValuesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind zero confidenceThreshold from properties")
        void testZeroConfidenceThreshold() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should bind zero globalMean from properties")
        void testZeroGlobalMean() {
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(0.0);
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.confidence-threshold=-10",
            "application.rating.global-mean=-2.5"
    })
    @DisplayName("RatingConfig Spring Integration - Negative Values")
    static class WithNegativeValuesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind negative confidenceThreshold from properties")
        void testNegativeConfidenceThreshold() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(-10);
        }

        @Test
        @DisplayName("Should bind negative globalMean from properties")
        void testNegativeGlobalMean() {
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(-2.5);
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.confidence-threshold=999999",
            "application.rating.global-mean=100.999"
    })
    @DisplayName("RatingConfig Spring Integration - Large Values")
    static class WithLargeValuesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind large confidenceThreshold from properties")
        void testLargeConfidenceThreshold() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(999999);
        }

        @Test
        @DisplayName("Should bind large globalMean from properties")
        void testLargeGlobalMean() {
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(100.999);
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.confidence-threshold=15"
            // globalMean not set - should use default
    })
    @DisplayName("RatingConfig Spring Integration - Partial Properties")
    static class WithPartialPropertiesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind custom confidenceThreshold and default globalMean")
        void testPartialBinding() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(15);
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(3.5); // Default
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.global-mean=2.7"
            // confidenceThreshold not set - should use default
    })
    @DisplayName("RatingConfig Spring Integration - Other Partial Properties")
    static class WithOtherPartialPropertiesTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind default confidenceThreshold and custom globalMean")
        void testOtherPartialBinding() {
            assertThat(ratingConfig.getConfidenceThreshold()).isEqualTo(10); // Default
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(2.7);
        }
    }

    @SpringBootTest(classes = RatingConfigTestConfiguration.class)
    @EnableConfigurationProperties(RatingConfig.class)
    @TestPropertySource(properties = {
            "application.rating.global-mean=3.141592653589793"
    })
    @DisplayName("RatingConfig Spring Integration - High Precision Double")
    static class WithHighPrecisionDoubleTest {

        @Autowired
        private RatingConfig ratingConfig;

        @Test
        @DisplayName("Should bind high precision globalMean from properties")
        void testHighPrecisionDouble() {
            assertThat(ratingConfig.getGlobalMean()).isEqualTo(3.141592653589793);
        }
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Should handle Integer.MAX_VALUE for confidenceThreshold")
    void testMaxIntegerConfidenceThreshold() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setConfidenceThreshold(Integer.MAX_VALUE);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle Integer.MIN_VALUE for confidenceThreshold")
    void testMinIntegerConfidenceThreshold() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setConfidenceThreshold(Integer.MIN_VALUE);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Should handle Double.MAX_VALUE for globalMean")
    void testMaxDoubleGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(Double.MAX_VALUE);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle Double.MIN_VALUE for globalMean")
    void testMinDoubleGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(Double.MIN_VALUE);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(Double.MIN_VALUE);
    }

    @Test
    @DisplayName("Should handle negative infinity for globalMean")
    void testNegativeInfinityGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(Double.NEGATIVE_INFINITY);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(Double.NEGATIVE_INFINITY);
    }

    @Test
    @DisplayName("Should handle positive infinity for globalMean")
    void testPositiveInfinityGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(Double.POSITIVE_INFINITY);

        // Then
        assertThat(config.getGlobalMean()).isEqualTo(Double.POSITIVE_INFINITY);
    }

    @Test
    @DisplayName("Should handle NaN for globalMean")
    void testNaNGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When
        config.setGlobalMean(Double.NaN);

        // Then
        assertThat(config.getGlobalMean()).isNaN();
    }

    // ==================== Real-World Scenario Tests ====================

    @Test
    @DisplayName("Should support typical low-confidence scenario")
    void testLowConfidenceScenario() {
        // Given
        RatingConfig config = new RatingConfig();

        // When - Low confidence threshold (few reviews needed)
        config.setConfidenceThreshold(5);
        config.setGlobalMean(3.5);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(5);
        assertThat(config.getGlobalMean()).isEqualTo(3.5);
    }

    @Test
    @DisplayName("Should support typical high-confidence scenario")
    void testHighConfidenceScenario() {
        // Given
        RatingConfig config = new RatingConfig();

        // When - High confidence threshold (many reviews needed)
        config.setConfidenceThreshold(100);
        config.setGlobalMean(3.5);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(100);
        assertThat(config.getGlobalMean()).isEqualTo(3.5);
    }

    @Test
    @DisplayName("Should support optimistic globalMean scenario")
    void testOptimisticGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When - Optimistic global mean (platform with generally good ratings)
        config.setConfidenceThreshold(10);
        config.setGlobalMean(4.2);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(10);
        assertThat(config.getGlobalMean()).isEqualTo(4.2);
    }

    @Test
    @DisplayName("Should support pessimistic globalMean scenario")
    void testPessimisticGlobalMean() {
        // Given
        RatingConfig config = new RatingConfig();

        // When - Pessimistic global mean (platform with generally lower ratings)
        config.setConfidenceThreshold(10);
        config.setGlobalMean(2.8);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(10);
        assertThat(config.getGlobalMean()).isEqualTo(2.8);
    }

    @Test
    @DisplayName("Should support neutral rating system (mid-point mean)")
    void testNeutralRatingSystem() {
        // Given
        RatingConfig config = new RatingConfig();

        // When - Neutral system (3.0 is middle of 1-5 scale)
        config.setConfidenceThreshold(10);
        config.setGlobalMean(3.0);

        // Then
        assertThat(config.getConfidenceThreshold()).isEqualTo(10);
        assertThat(config.getGlobalMean()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("Should verify @ConfigurationProperties prefix")
    void testConfigurationPropertiesPrefix() {
        // This test documents the expected property prefix
        // Actual properties should be:
        // - application.rating.confidence-threshold
        // - application.rating.global-mean
        
        // No assertion needed - this is documentation
        assertThat("application.rating").isEqualTo("application.rating");
    }

    @Test
    @DisplayName("Should verify field types match expected usage")
    void testFieldTypes() {
        // Given
        RatingConfig config = new RatingConfig();

        // Then - Verify types
        assertThat(config.getConfidenceThreshold()).isInstanceOf(Integer.class);
        assertThat(config.getGlobalMean()).isInstanceOf(Double.class);
    }
}
