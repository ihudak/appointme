package eu.dec21.appointme.businesses.businesses.mapper;

import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.entity.BusinessImage;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.common.entity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for BusinessMapper.
 * Tests mapping between BusinessRequest -> Business and Business -> BusinessResponse.
 */
@DisplayName("BusinessMapper Tests")
class BusinessMapperTest {

    private BusinessMapper mapper;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        mapper = new BusinessMapper();
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    // ==================== toBusiness() Tests ====================

    @Test
    @DisplayName("Should map BusinessRequest to Business with all fields")
    void testToBusiness_AllFields() {
        // Given
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                "A comprehensive description",
                address,
                location,
                "+491234567890",
                "https://example.com",
                "test@example.com"
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business).isNotNull();
        assertThat(business.getId()).isEqualTo(1L);
        assertThat(business.getName()).isEqualTo("Test Business");
        assertThat(business.getDescription()).isEqualTo("A comprehensive description");
        assertThat(business.getAddress()).isEqualTo(address);
        assertThat(business.getLocation()).isEqualTo(location);
        assertThat(business.getPhoneNumber()).isEqualTo("+491234567890");
        assertThat(business.getWebsite()).isEqualTo("https://example.com");
        assertThat(business.getEmail()).isEqualTo("test@example.com");
        assertThat(business.isActive()).isTrue(); // Default set to true
    }

    @Test
    @DisplayName("Should map BusinessRequest with minimal fields")
    void testToBusiness_MinimalFields() {
        // Given
        BusinessRequest request = new BusinessRequest(
                null,
                "Minimal Business",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business).isNotNull();
        assertThat(business.getId()).isNull();
        assertThat(business.getName()).isEqualTo("Minimal Business");
        assertThat(business.getDescription()).isNull();
        assertThat(business.getAddress()).isNull();
        assertThat(business.getLocation()).isNull();
        assertThat(business.getPhoneNumber()).isNull();
        assertThat(business.getWebsite()).isNull();
        assertThat(business.getEmail()).isNull();
        assertThat(business.isActive()).isTrue(); // Always set to true
    }

    @Test
    @DisplayName("Should always set active to true when mapping from request")
    void testToBusiness_ActiveAlwaysTrue() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "Active Business",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map request with complete Address")
    void testToBusiness_WithCompleteAddress() {
        // Given
        Address address = new Address(
                "123 Test St, Berlin, 10115, Germany",
                "123 Test Street",
                "Suite 4B",
                "Berlin",
                "Berlin",
                "10115",
                "DE",
                "ChIJAVkDPzdOqEcRcDteW0YgIQQ"
        );

        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                null,
                address,
                null,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business.getAddress()).isNotNull();
        assertThat(business.getAddress().getFormattedAddress()).isEqualTo("123 Test St, Berlin, 10115, Germany");
        assertThat(business.getAddress().getLine1()).isEqualTo("123 Test Street");
        assertThat(business.getAddress().getLine2()).isEqualTo("Suite 4B");
        assertThat(business.getAddress().getCity()).isEqualTo("Berlin");
        assertThat(business.getAddress().getRegion()).isEqualTo("Berlin");
        assertThat(business.getAddress().getPostalCode()).isEqualTo("10115");
        assertThat(business.getAddress().getCountryCode()).isEqualTo("DE");
        assertThat(business.getAddress().getPlaceId()).isEqualTo("ChIJAVkDPzdOqEcRcDteW0YgIQQ");
    }

    @Test
    @DisplayName("Should map request with Point location")
    void testToBusiness_WithLocation() {
        // Given
        Point location = createPoint(139.6917, 35.6895); // Tokyo coordinates

        BusinessRequest request = new BusinessRequest(
                1L,
                "Tokyo Business",
                null,
                null,
                location,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business.getLocation()).isNotNull();
        assertThat(business.getLocation().getX()).isEqualTo(139.6917);
        assertThat(business.getLocation().getY()).isEqualTo(35.6895);
        assertThat(business.getLocation().getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("Should map request with negative coordinates")
    void testToBusiness_WithNegativeCoordinates() {
        // Given
        Point location = createPoint(151.2093, -33.8688); // Sydney coordinates (negative latitude)

        BusinessRequest request = new BusinessRequest(
                1L,
                "Sydney Business",
                null,
                null,
                location,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business.getLocation()).isNotNull();
        assertThat(business.getLocation().getX()).isEqualTo(151.2093);
        assertThat(business.getLocation().getY()).isEqualTo(-33.8688);
    }

    @Test
    @DisplayName("Should map request with very long description")
    void testToBusiness_WithLongDescription() {
        // Given
        String longDescription = "x".repeat(2000);

        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                longDescription,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business.getDescription()).hasSize(2000);
        assertThat(business.getDescription()).isEqualTo(longDescription);
    }

    @Test
    @DisplayName("Should map request with special characters in fields")
    void testToBusiness_WithSpecialCharacters() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "Business & Co. <Test>",
                "Description with special chars: @#$%",
                null,
                null,
                "+49-123-456-7890",
                "https://example.com/path?query=value&other=123",
                "test+tag@example.com"
        );

        // When
        Business business = mapper.toBusiness(request);

        // Then
        assertThat(business.getName()).isEqualTo("Business & Co. <Test>");
        assertThat(business.getDescription()).isEqualTo("Description with special chars: @#$%");
        assertThat(business.getPhoneNumber()).isEqualTo("+49-123-456-7890");
        assertThat(business.getWebsite()).isEqualTo("https://example.com/path?query=value&other=123");
        assertThat(business.getEmail()).isEqualTo("test+tag@example.com");
    }

    @Test
    @DisplayName("Should create new Business instance each time")
    void testToBusiness_CreatesNewInstance() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Business business1 = mapper.toBusiness(request);
        Business business2 = mapper.toBusiness(request);

        // Then
        assertThat(business1).isNotSameAs(business2);
        assertThat(business1.getName()).isEqualTo(business2.getName());
    }

    // ==================== toBusinessResponse() Tests ====================

    @Test
    @DisplayName("Should map Business to BusinessResponse with all fields")
    void testToBusinessResponse_AllFields() {
        // Given
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        Business business = Business.builder()
                .id(1L)
                .name("Test Business")
                .description("Business description")
                .address(address)
                .location(location)
                .phoneNumber("+491234567890")
                .website("https://example.com")
                .email("test@example.com")
                .rating(4.5)
                .reviewCount(100)
                .active(true)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Business");
        assertThat(response.getDescription()).isEqualTo("Business description");
        assertThat(response.getAddress()).isEqualTo(address);
        assertThat(response.getLocation()).isEqualTo(location);
        assertThat(response.getPhoneNumber()).isEqualTo("+491234567890");
        assertThat(response.getWebsite()).isEqualTo("https://example.com");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRating()).isEqualTo(4.5);
        assertThat(response.getReviewCount()).isEqualTo(100);
        assertThat(response.isActive()).isTrue();
        assertThat(response.getImageUrl()).isNull(); // No images
    }

    @Test
    @DisplayName("Should map Business with minimal fields")
    void testToBusinessResponse_MinimalFields() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Minimal Business")
                .active(false)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Minimal Business");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getAddress()).isNull();
        assertThat(response.getLocation()).isNull();
        assertThat(response.getPhoneNumber()).isNull();
        assertThat(response.getWebsite()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getRating()).isNull();
        assertThat(response.getReviewCount()).isNull();
        assertThat(response.isActive()).isFalse();
        assertThat(response.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should map Business with no images to null imageUrl")
    void testToBusinessResponse_NoImages() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("No Images Business")
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should map Business with single icon image")
    void testToBusinessResponse_WithSingleIconImage() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Business with Icon")
                .images(new HashSet<>())
                .build();

        BusinessImage iconImage = createBusinessImage(business, "https://example.com/icon.png", true, 0);
        business.getImages().add(iconImage);

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/icon.png");
    }

    @Test
    @DisplayName("Should map Business with multiple icon images and select lowest display order")
    void testToBusinessResponse_WithMultipleIconImages_SelectsLowestDisplayOrder() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Business with Multiple Icons")
                .images(new HashSet<>())
                .build();

        BusinessImage icon1 = createBusinessImage(business, "https://example.com/icon1.png", true, 5);
        BusinessImage icon2 = createBusinessImage(business, "https://example.com/icon2.png", true, 1);
        BusinessImage icon3 = createBusinessImage(business, "https://example.com/icon3.png", true, 10);

        business.getImages().add(icon1);
        business.getImages().add(icon2);
        business.getImages().add(icon3);

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then - Should select icon2 with display order 1
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/icon2.png");
    }

    @Test
    @DisplayName("Should map Business with mixed icon and non-icon images")
    void testToBusinessResponse_WithMixedImages_OnlyConsidersIcons() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Business with Mixed Images")
                .images(new HashSet<>())
                .build();

        BusinessImage regularImage1 = createBusinessImage(business, "https://example.com/regular1.png", false, 0);
        BusinessImage iconImage = createBusinessImage(business, "https://example.com/icon.png", true, 5);
        BusinessImage regularImage2 = createBusinessImage(business, "https://example.com/regular2.png", false, 1);

        business.getImages().add(regularImage1);
        business.getImages().add(iconImage);
        business.getImages().add(regularImage2);

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then - Should select only the icon image
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/icon.png");
    }

    @Test
    @DisplayName("Should map Business with only non-icon images to null imageUrl")
    void testToBusinessResponse_WithOnlyNonIconImages() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Business with Non-Icon Images")
                .images(new HashSet<>())
                .build();

        BusinessImage regularImage1 = createBusinessImage(business, "https://example.com/regular1.png", false, 0);
        BusinessImage regularImage2 = createBusinessImage(business, "https://example.com/regular2.png", false, 1);

        business.getImages().add(regularImage1);
        business.getImages().add(regularImage2);

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then - No icon images, so imageUrl should be null
        assertThat(response.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should handle icon images with same display order")
    void testToBusinessResponse_WithSameDisplayOrder() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Business with Same Display Order")
                .images(new HashSet<>())
                .build();

        // Multiple icons with same display order - min() will pick one deterministically
        BusinessImage icon1 = createBusinessImage(business, "https://example.com/icon1.png", true, 5);
        BusinessImage icon2 = createBusinessImage(business, "https://example.com/icon2.png", true, 5);
        BusinessImage icon3 = createBusinessImage(business, "https://example.com/icon3.png", true, 5);

        business.getImages().add(icon1);
        business.getImages().add(icon2);
        business.getImages().add(icon3);

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then - Should return one of them (deterministic based on Comparator)
        assertThat(response.getImageUrl()).isNotNull();
        assertThat(response.getImageUrl()).isIn(
                "https://example.com/icon1.png",
                "https://example.com/icon2.png",
                "https://example.com/icon3.png"
        );
    }

    @Test
    @DisplayName("Should handle icon images with negative display order")
    void testToBusinessResponse_WithNegativeDisplayOrder() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Business with Negative Display Order")
                .images(new HashSet<>())
                .build();

        BusinessImage icon1 = createBusinessImage(business, "https://example.com/icon1.png", true, 0);
        BusinessImage icon2 = createBusinessImage(business, "https://example.com/icon2.png", true, -5);
        BusinessImage icon3 = createBusinessImage(business, "https://example.com/icon3.png", true, 10);

        business.getImages().add(icon1);
        business.getImages().add(icon2);
        business.getImages().add(icon3);

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then - Should select icon2 with display order -5 (lowest)
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/icon2.png");
    }

    @Test
    @DisplayName("Should map Business with null rating")
    void testToBusinessResponse_WithNullRating() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("No Rating Business")
                .rating(null)
                .reviewCount(null)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getRating()).isNull();
        assertThat(response.getReviewCount()).isNull();
    }

    @Test
    @DisplayName("Should map Business with zero rating")
    void testToBusinessResponse_WithZeroRating() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Zero Rating Business")
                .rating(0.0)
                .reviewCount(0)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getRating()).isEqualTo(0.0);
        assertThat(response.getReviewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should map Business with maximum rating")
    void testToBusinessResponse_WithMaximumRating() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Max Rating Business")
                .rating(5.0)
                .reviewCount(Integer.MAX_VALUE)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getRating()).isEqualTo(5.0);
        assertThat(response.getReviewCount()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Should map Business with fractional rating")
    void testToBusinessResponse_WithFractionalRating() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Fractional Rating Business")
                .rating(3.7)
                .reviewCount(42)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getRating()).isEqualTo(3.7);
        assertThat(response.getReviewCount()).isEqualTo(42);
    }

    @Test
    @DisplayName("Should map Business preserving active false state")
    void testToBusinessResponse_ActiveFalse() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Inactive Business")
                .active(false)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should map Business with complete address details")
    void testToBusinessResponse_WithCompleteAddress() {
        // Given
        Address address = new Address(
                "456 Main St, Munich, 80331, Germany",
                "456 Main Street",
                "Floor 3",
                "Munich",
                "Bavaria",
                "80331",
                "DE",
                "ChIJ2V-Mo_l1nkcRfZixfUq4DAE"
        );

        Business business = Business.builder()
                .id(1L)
                .name("Munich Business")
                .address(address)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getAddress()).isNotNull();
        assertThat(response.getAddress().getFormattedAddress()).isEqualTo("456 Main St, Munich, 80331, Germany");
        assertThat(response.getAddress().getLine1()).isEqualTo("456 Main Street");
        assertThat(response.getAddress().getLine2()).isEqualTo("Floor 3");
        assertThat(response.getAddress().getCity()).isEqualTo("Munich");
        assertThat(response.getAddress().getRegion()).isEqualTo("Bavaria");
        assertThat(response.getAddress().getPostalCode()).isEqualTo("80331");
        assertThat(response.getAddress().getCountryCode()).isEqualTo("DE");
        assertThat(response.getAddress().getPlaceId()).isEqualTo("ChIJ2V-Mo_l1nkcRfZixfUq4DAE");
    }

    @Test
    @DisplayName("Should map Business location preserving coordinates")
    void testToBusinessResponse_WithLocation() {
        // Given
        Point location = createPoint(-0.1278, 51.5074); // London coordinates

        Business business = Business.builder()
                .id(1L)
                .name("London Business")
                .location(location)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getLocation()).isNotNull();
        assertThat(response.getLocation().getX()).isEqualTo(-0.1278);
        assertThat(response.getLocation().getY()).isEqualTo(51.5074);
    }

    @Test
    @DisplayName("Should create new BusinessResponse instance each time")
    void testToBusinessResponse_CreatesNewInstance() {
        // Given
        Business business = Business.builder()
                .id(1L)
                .name("Test Business")
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response1 = mapper.toBusinessResponse(business);
        BusinessResponse response2 = mapper.toBusinessResponse(business);

        // Then
        assertThat(response1).isNotSameAs(response2);
        assertThat(response1.getName()).isEqualTo(response2.getName());
    }

    @Test
    @DisplayName("Should handle Business entity fields not present in request")
    void testToBusinessResponse_WithEntitySpecificFields() {
        // Given - Business has additional fields like createdAt, updatedAt from BaseEntity
        Business business = Business.builder()
                .id(1L)
                .name("Entity Business")
                .ownerId(100L)
                .emailVerified(true)
                .weightedRating(4.2)
                .images(new HashSet<>())
                .build();

        // When
        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then - Response should only contain mapped fields
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Entity Business");
        // ownerId, emailVerified, weightedRating are not in response
    }

    // ==================== Round-trip Tests ====================

    @Test
    @DisplayName("Should preserve basic data in request -> entity -> response round-trip")
    void testRoundTrip_BasicFields() {
        // Given
        BusinessRequest request = new BusinessRequest(
                null, // ID will be generated by DB
                "Round Trip Business",
                "Description for round trip",
                null,
                null,
                "+491234567890",
                "https://example.com",
                "roundtrip@example.com"
        );

        // When
        Business business = mapper.toBusiness(request);
        business.setId(99L); // Simulate DB-generated ID
        business.setRating(4.0);
        business.setReviewCount(50);
        business.setImages(new HashSet<>());

        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getName()).isEqualTo("Round Trip Business");
        assertThat(response.getDescription()).isEqualTo("Description for round trip");
        assertThat(response.getPhoneNumber()).isEqualTo("+491234567890");
        assertThat(response.getWebsite()).isEqualTo("https://example.com");
        assertThat(response.getEmail()).isEqualTo("roundtrip@example.com");
        assertThat(response.isActive()).isTrue(); // Set by mapper
        assertThat(response.getRating()).isEqualTo(4.0);
        assertThat(response.getReviewCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should preserve address in round-trip")
    void testRoundTrip_WithAddress() {
        // Given
        Address address = createAddress();

        BusinessRequest request = new BusinessRequest(
                null,
                "Address Business",
                null,
                address,
                null,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);
        business.setId(1L);
        business.setImages(new HashSet<>());

        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getAddress()).isNotNull();
        assertThat(response.getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(response.getAddress().getCountryCode()).isEqualTo(address.getCountryCode());
    }

    @Test
    @DisplayName("Should preserve location in round-trip")
    void testRoundTrip_WithLocation() {
        // Given
        Point location = createPoint(2.3522, 48.8566); // Paris coordinates

        BusinessRequest request = new BusinessRequest(
                null,
                "Paris Business",
                null,
                null,
                location,
                null,
                null,
                null
        );

        // When
        Business business = mapper.toBusiness(request);
        business.setId(1L);
        business.setImages(new HashSet<>());

        BusinessResponse response = mapper.toBusinessResponse(business);

        // Then
        assertThat(response.getLocation()).isNotNull();
        assertThat(response.getLocation().getX()).isEqualTo(2.3522);
        assertThat(response.getLocation().getY()).isEqualTo(48.8566);
    }

    // ==================== Helper Methods ====================

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

    private BusinessImage createBusinessImage(Business business, String imageUrl, boolean isIcon, int displayOrder) {
        return BusinessImage.builder()
                .business(business)
                .imageUrl(imageUrl)
                .isIcon(isIcon)
                .displayOrder(displayOrder)
                .build();
    }
}
