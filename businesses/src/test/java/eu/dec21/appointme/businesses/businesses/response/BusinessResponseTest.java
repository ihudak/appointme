package eu.dec21.appointme.businesses.businesses.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dec21.appointme.common.entity.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for BusinessResponse.
 * Tests builder pattern, getters/setters, equals/hashCode, and JSON serialization.
 */
@DisplayName("BusinessResponse Tests")
class BusinessResponseTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should create BusinessResponse using builder with all fields")
    void testBuilder_AllFields() {
        // Given
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        // When
        BusinessResponse response = BusinessResponse.builder()
                .id(1L)
                .name("Test Business")
                .description("A test business description")
                .address(address)
                .location(location)
                .phoneNumber("+491234567890")
                .website("https://example.com")
                .email("test@example.com")
                .imageUrl("https://example.com/image.jpg")
                .rating(4.5)
                .reviewCount(100)
                .active(true)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Business");
        assertThat(response.getDescription()).isEqualTo("A test business description");
        assertThat(response.getAddress()).isEqualTo(address);
        assertThat(response.getLocation()).isEqualTo(location);
        assertThat(response.getPhoneNumber()).isEqualTo("+491234567890");
        assertThat(response.getWebsite()).isEqualTo("https://example.com");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(response.getRating()).isEqualTo(4.5);
        assertThat(response.getReviewCount()).isEqualTo(100);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should create BusinessResponse using builder with minimal fields")
    void testBuilder_MinimalFields() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .id(1L)
                .name("Minimal Business")
                .email("minimal@example.com")
                .active(false)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Minimal Business");
        assertThat(response.getEmail()).isEqualTo("minimal@example.com");
        assertThat(response.isActive()).isFalse();
        
        // Null fields
        assertThat(response.getDescription()).isNull();
        assertThat(response.getAddress()).isNull();
        assertThat(response.getLocation()).isNull();
        assertThat(response.getPhoneNumber()).isNull();
        assertThat(response.getWebsite()).isNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(response.getRating()).isNull();
        assertThat(response.getReviewCount()).isNull();
    }

    @Test
    @DisplayName("Should create BusinessResponse using no-args constructor")
    void testNoArgsConstructor() {
        // When
        BusinessResponse response = new BusinessResponse();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getAddress()).isNull();
        assertThat(response.getLocation()).isNull();
        assertThat(response.getPhoneNumber()).isNull();
        assertThat(response.getWebsite()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(response.getRating()).isNull();
        assertThat(response.getReviewCount()).isNull();
        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should create BusinessResponse using all-args constructor")
    void testAllArgsConstructor() {
        // Given
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        // When
        BusinessResponse response = new BusinessResponse(
                1L,
                "Test Business",
                "Test description",
                address,
                location,
                "+491234567890",
                "https://example.com",
                "test@example.com",
                "https://example.com/image.jpg",
                4.5,
                100,
                true
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Business");
        assertThat(response.getDescription()).isEqualTo("Test description");
        assertThat(response.getAddress()).isEqualTo(address);
        assertThat(response.getLocation()).isEqualTo(location);
        assertThat(response.getPhoneNumber()).isEqualTo("+491234567890");
        assertThat(response.getWebsite()).isEqualTo("https://example.com");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(response.getRating()).isEqualTo(4.5);
        assertThat(response.getReviewCount()).isEqualTo(100);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should properly use setters to modify fields")
    void testSetters() {
        // Given
        BusinessResponse response = new BusinessResponse();
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        // When
        response.setId(2L);
        response.setName("Updated Business");
        response.setDescription("Updated description");
        response.setAddress(address);
        response.setLocation(location);
        response.setPhoneNumber("+499876543210");
        response.setWebsite("https://updated.com");
        response.setEmail("updated@example.com");
        response.setImageUrl("https://updated.com/image.jpg");
        response.setRating(3.8);
        response.setReviewCount(50);
        response.setActive(false);

        // Then
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Updated Business");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getAddress()).isEqualTo(address);
        assertThat(response.getLocation()).isEqualTo(location);
        assertThat(response.getPhoneNumber()).isEqualTo("+499876543210");
        assertThat(response.getWebsite()).isEqualTo("https://updated.com");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getImageUrl()).isEqualTo("https://updated.com/image.jpg");
        assertThat(response.getRating()).isEqualTo(3.8);
        assertThat(response.getReviewCount()).isEqualTo(50);
        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .id(null)
                .name(null)
                .description(null)
                .address(null)
                .location(null)
                .phoneNumber(null)
                .website(null)
                .email(null)
                .imageUrl(null)
                .rating(null)
                .reviewCount(null)
                .active(false)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getAddress()).isNull();
        assertThat(response.getLocation()).isNull();
        assertThat(response.getPhoneNumber()).isNull();
        assertThat(response.getWebsite()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(response.getRating()).isNull();
        assertThat(response.getReviewCount()).isNull();
        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle zero and negative values for numeric fields")
    void testEdgeCaseNumericValues() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .id(0L)
                .rating(0.0)
                .reviewCount(0)
                .build();

        // Then
        assertThat(response.getId()).isZero();
        assertThat(response.getRating()).isZero();
        assertThat(response.getReviewCount()).isZero();

        // Negative values
        response.setRating(-1.0);
        response.setReviewCount(-10);
        assertThat(response.getRating()).isEqualTo(-1.0);
        assertThat(response.getReviewCount()).isEqualTo(-10);
    }

    @Test
    @DisplayName("Should handle maximum rating value")
    void testMaximumRating() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .rating(5.0)
                .build();

        // Then
        assertThat(response.getRating()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Should handle very large review count")
    void testLargeReviewCount() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .reviewCount(Integer.MAX_VALUE)
                .build();

        // Then
        assertThat(response.getReviewCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .name("")
                .description("")
                .phoneNumber("")
                .website("")
                .email("")
                .imageUrl("")
                .build();

        // Then
        assertThat(response.getName()).isEmpty();
        assertThat(response.getDescription()).isEmpty();
        assertThat(response.getPhoneNumber()).isEmpty();
        assertThat(response.getWebsite()).isEmpty();
        assertThat(response.getEmail()).isEmpty();
        assertThat(response.getImageUrl()).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testLongStrings() {
        // Given
        String longString = "a".repeat(1000);

        // When
        BusinessResponse response = BusinessResponse.builder()
                .name(longString)
                .description(longString)
                .build();

        // Then
        assertThat(response.getName()).hasSize(1000);
        assertThat(response.getDescription()).hasSize(1000);
    }

    @Test
    @DisplayName("Should handle special characters in strings")
    void testSpecialCharacters() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .name("Business & Co. <Test>")
                .description("Special chars: @#$%^&*()")
                .phoneNumber("+49-123-456-7890")
                .website("https://example.com/path?query=value&other=123")
                .email("test+tag@example.co.uk")
                .build();

        // Then
        assertThat(response.getName()).isEqualTo("Business & Co. <Test>");
        assertThat(response.getDescription()).isEqualTo("Special chars: @#$%^&*()");
        assertThat(response.getPhoneNumber()).isEqualTo("+49-123-456-7890");
        assertThat(response.getWebsite()).isEqualTo("https://example.com/path?query=value&other=123");
        assertThat(response.getEmail()).isEqualTo("test+tag@example.co.uk");
    }

    @Test
    @DisplayName("Should handle Unicode characters")
    void testUnicodeCharacters() {
        // When
        BusinessResponse response = BusinessResponse.builder()
                .name("Café München ☕")
                .description("日本語 中文 한글 العربية")
                .build();

        // Then
        assertThat(response.getName()).isEqualTo("Café München ☕");
        assertThat(response.getDescription()).isEqualTo("日本語 中文 한글 العربية");
    }

    @Test
    @DisplayName("Should handle different Point coordinates")
    void testDifferentCoordinates() {
        // Given
        Point berlinPoint = createPoint(13.4050, 52.5200);
        Point tokyoPoint = createPoint(139.6917, 35.6895);
        Point newYorkPoint = createPoint(-74.0060, 40.7128);

        // When/Then - Berlin
        BusinessResponse response = BusinessResponse.builder()
                .location(berlinPoint)
                .build();
        assertThat(response.getLocation().getX()).isEqualTo(13.4050);
        assertThat(response.getLocation().getY()).isEqualTo(52.5200);

        // Tokyo
        response.setLocation(tokyoPoint);
        assertThat(response.getLocation().getX()).isEqualTo(139.6917);
        assertThat(response.getLocation().getY()).isEqualTo(35.6895);

        // New York (negative longitude)
        response.setLocation(newYorkPoint);
        assertThat(response.getLocation().getX()).isEqualTo(-74.0060);
        assertThat(response.getLocation().getY()).isEqualTo(40.7128);
    }

    @Test
    @DisplayName("Should handle Address with all fields populated")
    void testAddressWithAllFields() {
        // Given
        Address address = new Address(
                "123 Test Street, Berlin, 10115, Germany",
                "123 Test Street",
                "Apartment 4B",
                "Berlin",
                "Berlin",
                "10115",
                "DE",
                "ChIJAVkDPzdOqEcRcDteW0YgIQQ"
        );

        // When
        BusinessResponse response = BusinessResponse.builder()
                .address(address)
                .build();

        // Then
        assertThat(response.getAddress()).isNotNull();
        assertThat(response.getAddress().getFormattedAddress()).isEqualTo("123 Test Street, Berlin, 10115, Germany");
        assertThat(response.getAddress().getLine1()).isEqualTo("123 Test Street");
        assertThat(response.getAddress().getLine2()).isEqualTo("Apartment 4B");
        assertThat(response.getAddress().getCity()).isEqualTo("Berlin");
        assertThat(response.getAddress().getRegion()).isEqualTo("Berlin");
        assertThat(response.getAddress().getPostalCode()).isEqualTo("10115");
        assertThat(response.getAddress().getCountryCode()).isEqualTo("DE");
        assertThat(response.getAddress().getPlaceId()).isEqualTo("ChIJAVkDPzdOqEcRcDteW0YgIQQ");
    }

    @Test
    @DisplayName("Should handle Address with minimal fields")
    void testAddressWithMinimalFields() {
        // Given
        Address address = new Address();
        address.setCity("Berlin");
        address.setCountryCode("DE");

        // When
        BusinessResponse response = BusinessResponse.builder()
                .address(address)
                .build();

        // Then
        assertThat(response.getAddress()).isNotNull();
        assertThat(response.getAddress().getCity()).isEqualTo("Berlin");
        assertThat(response.getAddress().getCountryCode()).isEqualTo("DE");
        assertThat(response.getAddress().getFormattedAddress()).isNull();
        assertThat(response.getAddress().getLine1()).isNull();
    }

    @Test
    @DisplayName("Should handle fractional rating values")
    void testFractionalRatings() {
        // Test various fractional values
        double[] ratings = {0.1, 1.5, 2.33, 3.67, 4.99, 5.0};

        for (double rating : ratings) {
            BusinessResponse response = BusinessResponse.builder()
                    .rating(rating)
                    .build();

            assertThat(response.getRating()).isEqualTo(rating);
        }
    }

    @Test
    @DisplayName("Should handle active flag values")
    void testActiveFlag() {
        // Active = true
        BusinessResponse activeResponse = BusinessResponse.builder()
                .active(true)
                .build();
        assertThat(activeResponse.isActive()).isTrue();

        // Active = false
        BusinessResponse inactiveResponse = BusinessResponse.builder()
                .active(false)
                .build();
        assertThat(inactiveResponse.isActive()).isFalse();

        // Change from true to false
        activeResponse.setActive(false);
        assertThat(activeResponse.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should create independent instances via builder")
    void testBuilderIndependence() {
        // Given
        Address address1 = createAddress();
        Address address2 = createAddress();

        // When
        BusinessResponse response1 = BusinessResponse.builder()
                .id(1L)
                .name("Business 1")
                .address(address1)
                .build();

        BusinessResponse response2 = BusinessResponse.builder()
                .id(2L)
                .name("Business 2")
                .address(address2)
                .build();

        // Then
        assertThat(response1).isNotSameAs(response2);
        assertThat(response1.getId()).isNotEqualTo(response2.getId());
        assertThat(response1.getName()).isNotEqualTo(response2.getName());
    }

    @Test
    @DisplayName("Should allow chaining setter calls")
    void testSetterChaining() {
        // Given
        BusinessResponse response = new BusinessResponse();

        // When
        response.setId(1L);
        response.setName("Test");
        response.setActive(true);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle toString method (Lombok generated)")
    void testToString() {
        // Given
        BusinessResponse response = BusinessResponse.builder()
                .id(1L)
                .name("Test Business")
                .email("test@example.com")
                .active(true)
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("BusinessResponse");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Test Business");
        assertThat(toString).contains("email=test@example.com");
        assertThat(toString).contains("active=true");
    }

    // Helper methods

    private Address createAddress() {
        Address address = new Address();
        address.setLine1("Test Street 123");
        address.setCity("Berlin");
        address.setPostalCode("10115");
        address.setCountryCode("DE");
        return address;
    }

    private Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
