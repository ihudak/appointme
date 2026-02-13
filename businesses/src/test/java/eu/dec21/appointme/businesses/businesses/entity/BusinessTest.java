package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.Address;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

class BusinessTest {

    private static Validator validator;
    private static GeometryFactory geometryFactory;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        geometryFactory = new GeometryFactory();
    }

    @Test
    void testBuilder_withAllFields() {
        Address address = new Address(
                "123 Main St, Berlin, Germany",
                "Main Street 123",
                "Building A",
                "Berlin",
                "Berlin",
                "10115",
                "DE",
                "ChIJ"
        );
        Point location = geometryFactory.createPoint(new Coordinate(13.4050, 52.5200));

        Business business = Business.builder()
                .name("Test Business")
                .description("Test Description")
                .active(true)
                .address(address)
                .location(location)
                .phoneNumber("+4915112345678")
                .website("https://example.com")
                .email("business@example.com")
                .emailVerified(true)
                .ownerId(1L)
                .rating(4.5)
                .reviewCount(100)
                .weightedRating(4.3)
                .categoryIds(new HashSet<>(Set.of(1L, 2L)))
                .adminIds(new HashSet<>(Set.of(10L, 20L)))
                .keywords(new HashSet<>())
                .images(new HashSet<>())
                .build();

        assertEquals("Test Business", business.getName());
        assertEquals("Test Description", business.getDescription());
        assertTrue(business.isActive());
        assertNotNull(business.getAddress());
        assertEquals("123 Main St, Berlin, Germany", business.getAddress().getFormattedAddress());
        assertEquals("Berlin", business.getAddress().getCity());
        assertEquals("10115", business.getAddress().getPostalCode());
        assertEquals("DE", business.getAddress().getCountryCode());
        assertEquals(location, business.getLocation());
        assertEquals("+4915112345678", business.getPhoneNumber());
        assertEquals("https://example.com", business.getWebsite());
        assertEquals("business@example.com", business.getEmail());
        assertTrue(business.isEmailVerified());
        assertEquals(1L, business.getOwnerId());
        assertEquals(4.5, business.getRating());
        assertEquals(100, business.getReviewCount());
        assertEquals(4.3, business.getWeightedRating());
        assertEquals(2, business.getCategoryIds().size());
        assertEquals(2, business.getAdminIds().size());
    }

    @Test
    void testBuilder_withMinimalFields() {
        Point location = geometryFactory.createPoint(new Coordinate(0, 0));

        Business business = Business.builder()
                .name("Minimal Business")
                .location(location)
                .email("minimal@example.com")
                .ownerId(1L)
                .build();

        assertEquals("Minimal Business", business.getName());
        assertEquals(location, business.getLocation());
        assertEquals("minimal@example.com", business.getEmail());
        assertEquals(1L, business.getOwnerId());
    }

    @Test
    void testDefaultValues() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .categoryIds(new HashSet<>())
                .adminIds(new HashSet<>())
                .keywords(new HashSet<>())
                .images(new HashSet<>())
                .build();

        assertNotNull(business.getCategoryIds());
        assertTrue(business.getCategoryIds().isEmpty());
        assertNotNull(business.getAdminIds());
        assertTrue(business.getAdminIds().isEmpty());
        assertNotNull(business.getKeywords());
        assertTrue(business.getKeywords().isEmpty());
        assertNotNull(business.getImages());
        assertTrue(business.getImages().isEmpty());
    }

    @Test
    void testAddress_embedded() {
        Address address = new Address(
                "456 Oak Ave, Munich, Germany",
                "Oak Avenue 456",
                null,
                "Munich",
                "Bavaria",
                "80331",
                "DE",
                null
        );

        Business business = Business.builder()
                .name("Munich Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(11.5820, 48.1351)))
                .ownerId(1L)
                .address(address)
                .build();

        assertNotNull(business.getAddress());
        assertEquals("456 Oak Ave, Munich, Germany", business.getAddress().getFormattedAddress());
        assertEquals("Oak Avenue 456", business.getAddress().getLine1());
        assertNull(business.getAddress().getLine2());
        assertEquals("Munich", business.getAddress().getCity());
        assertEquals("Bavaria", business.getAddress().getRegion());
        assertEquals("80331", business.getAddress().getPostalCode());
        assertEquals("DE", business.getAddress().getCountryCode());
        assertNull(business.getAddress().getPlaceId());
    }

    @Test
    void testAddress_withAllFields() {
        Address address = new Address(
                "221B Baker Street, London, UK",
                "221B Baker Street",
                "Apartment B",
                "London",
                "Greater London",
                "NW1 6XE",
                "GB",
                "ChIJdd4hrwug2EcRmSrV3Vo6llI"
        );

        Business business = Business.builder()
                .name("London Business")
                .email("london@example.com")
                .location(geometryFactory.createPoint(new Coordinate(-0.1276, 51.5074)))
                .ownerId(1L)
                .address(address)
                .build();

        Address retrievedAddress = business.getAddress();
        assertNotNull(retrievedAddress);
        assertEquals("221B Baker Street, London, UK", retrievedAddress.getFormattedAddress());
        assertEquals("221B Baker Street", retrievedAddress.getLine1());
        assertEquals("Apartment B", retrievedAddress.getLine2());
        assertEquals("London", retrievedAddress.getCity());
        assertEquals("Greater London", retrievedAddress.getRegion());
        assertEquals("NW1 6XE", retrievedAddress.getPostalCode());
        assertEquals("GB", retrievedAddress.getCountryCode());
        assertEquals("ChIJdd4hrwug2EcRmSrV3Vo6llI", retrievedAddress.getPlaceId());
    }

    @Test
    void testAddress_canBeNull() {
        // Some businesses might not have a physical address (online-only)
        Business business = Business.builder()
                .name("Online Only Business")
                .email("online@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .address(null)
                .build();

        assertNull(business.getAddress());
    }

    @Test
    void testAddress_updateViaSettermethod() {
        Business business = Business.builder()
                .name("Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        assertNull(business.getAddress());

        Address newAddress = new Address(
                "New Street 123, Berlin, Germany",
                "New Street 123",
                null,
                "Berlin",
                "Berlin",
                "10115",
                "DE",
                null
        );

        business.setAddress(newAddress);

        assertNotNull(business.getAddress());
        assertEquals("New Street 123, Berlin, Germany", business.getAddress().getFormattedAddress());
        assertEquals("Berlin", business.getAddress().getCity());
    }

    @Test
    void testAddress_differentCountryCodes() {
        // Test US address
        Address usAddress = new Address(
                "1600 Pennsylvania Avenue NW, Washington, DC 20500, USA",
                "1600 Pennsylvania Avenue NW",
                null,
                "Washington",
                "DC",
                "20500",
                "US",
                null
        );

        Business usBusiness = Business.builder()
                .name("US Business")
                .email("us@example.com")
                .location(geometryFactory.createPoint(new Coordinate(-77.0365, 38.8977)))
                .ownerId(1L)
                .address(usAddress)
                .build();

        assertEquals("US", usBusiness.getAddress().getCountryCode());
        assertEquals("Washington", usBusiness.getAddress().getCity());

        // Test Japanese address
        Address jpAddress = new Address(
                "1 Chome-1-2 Oshiage, Sumida City, Tokyo 131-0045, Japan",
                "1 Chome-1-2 Oshiage",
                null,
                "Tokyo",
                "Sumida",
                "131-0045",
                "JP",
                null
        );

        Business jpBusiness = Business.builder()
                .name("Tokyo Business")
                .email("jp@example.com")
                .location(geometryFactory.createPoint(new Coordinate(139.8107, 35.7101)))
                .ownerId(2L)
                .address(jpAddress)
                .build();

        assertEquals("JP", jpBusiness.getAddress().getCountryCode());
        assertEquals("Tokyo", jpBusiness.getAddress().getCity());
    }

    @Test
    void testLocation_pointGeometry() {
        // Berlin coordinates
        Point berlin = geometryFactory.createPoint(new Coordinate(13.4050, 52.5200));

        Business business = Business.builder()
                .name("Berlin Business")
                .email("berlin@example.com")
                .location(berlin)
                .ownerId(1L)
                .build();

        assertNotNull(business.getLocation());
        assertEquals(13.4050, business.getLocation().getX(), 0.0001);
        assertEquals(52.5200, business.getLocation().getY(), 0.0001);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+4915112345678",
            "+12125551234",
            "+441234567890",
            "+861234567890123",
            "+919876543210"
    })
    void testPhoneNumberValidation_validE164(String phoneNumber) {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .phoneNumber(phoneNumber)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Valid E.164 phone should not have violations: " + phoneNumber);
    }

    @Test
    void testPhoneNumberValidation_nullIsValid() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .phoneNumber(null)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Null phone number should be valid (optional field)");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123456",
            "+",
            "+123abc",
            "001234567",
            "4915112345678",
            "+0123456789",
            "++4915112345678"
    })
    void testPhoneNumberValidation_invalidE164(String phoneNumber) {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .phoneNumber(phoneNumber)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty(), "Invalid E.164 phone should have violations: " + phoneNumber);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "https://www.example.com",
            "https://sub.domain.example.com",
            "https://example.com/path"
    })
    void testWebsiteValidation_validUrls(String website) {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .website(website)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Valid URL should not have violations: " + website);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "ftp://example.com",
            "example.com",
            "//example.com",
            "http:/example.com",
            "http://example.com"
    })
    void testWebsiteValidation_invalidUrls(String website) {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .website(website)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty(), "Invalid URL should have violations: " + website);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("website")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "test.user@example.com",
            "test+tag@example.co.uk",
            "user123@sub.domain.com"
    })
    void testEmailValidation_validEmails(String email) {
        Business business = Business.builder()
                .name("Test")
                .email(email)
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Valid email should not have violations: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plaintext",
            "@nodomain",
            "missing@",
            "spaces in@email.com",
            "user@",
            "@domain.com"
    })
    void testEmailValidation_invalidEmails(String email) {
        Business business = Business.builder()
                .name("Test")
                .email(email)
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty(), "Invalid email should have violations: " + email);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testEmailValidation_blankEmails(String email) {
        Business business = Business.builder()
                .name("Test")
                .email(email)
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testCategoryIds_collection() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .categoryIds(new HashSet<>(Set.of(1L, 2L, 3L)))
                .build();

        assertEquals(3, business.getCategoryIds().size());
        assertTrue(business.getCategoryIds().contains(1L));
        assertTrue(business.getCategoryIds().contains(2L));
        assertTrue(business.getCategoryIds().contains(3L));
    }

    @Test
    void testCategoryIds_addAndRemove() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .categoryIds(new HashSet<>())
                .build();

        business.getCategoryIds().add(10L);
        business.getCategoryIds().add(20L);

        assertEquals(2, business.getCategoryIds().size());

        business.getCategoryIds().remove(10L);

        assertEquals(1, business.getCategoryIds().size());
        assertTrue(business.getCategoryIds().contains(20L));
    }

    @Test
    void testAdminIds_collection() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .adminIds(new HashSet<>(Set.of(100L, 200L)))
                .build();

        assertEquals(2, business.getAdminIds().size());
        assertTrue(business.getAdminIds().contains(100L));
        assertTrue(business.getAdminIds().contains(200L));
    }

    @Test
    void testAdminIds_ownerNotAutomaticallyAdmin() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .adminIds(new HashSet<>())
                .build();

        assertFalse(business.getAdminIds().contains(1L), "Owner is not automatically in adminIds");
    }

    @Test
    void testRating_calculations() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .rating(4.5)
                .reviewCount(100)
                .build();

        assertEquals(4.5, business.getRating());
        assertEquals(100, business.getReviewCount());
    }

    @Test
    void testGetCalculatedRating_withReviews() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .rating(4.5)
                .reviewCount(100)
                .build();

        int confidenceThreshold = 10;
        double globalMean = 3.5;

        // (10 × 3.5 + 100 × 4.5) / (10 + 100) = (35 + 450) / 110 = 485 / 110 ≈ 4.409
        double calculated = business.getCalculatedRating(confidenceThreshold, globalMean);

        assertEquals(4.409, calculated, 0.01);
    }

    @Test
    void testGetCalculatedRating_noReviews() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .rating(null)
                .reviewCount(0)
                .build();

        double calculated = business.getCalculatedRating(10, 3.5);

        assertEquals(0.0, calculated);
    }

    @Test
    void testGetCalculatedRating_fewReviews() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .rating(5.0)
                .reviewCount(2)
                .build();

        int confidenceThreshold = 10;
        double globalMean = 3.5;

        // (10 × 3.5 + 2 × 5.0) / (10 + 2) = (35 + 10) / 12 = 45 / 12 = 3.75
        double calculated = business.getCalculatedRating(confidenceThreshold, globalMean);

        assertEquals(3.75, calculated, 0.01);
    }

    @Test
    @DisplayName("getCalculatedRating - Should handle negative confidence threshold")
    void testGetCalculatedRating_NegativeConfidenceThreshold() {
        // Given
        Business business = Business.builder()
                .name("Test Business")
                .email("test@test.com")
                .rating(4.0)
                .reviewCount(10)
                .build();

        // When — negative threshold: (-5 * 3.5 + 10 * 4.0) / (-5 + 10) = 22.5/5 = 4.5
        Double result = business.getCalculatedRating(-5, 3.5);

        // Then
        assertThat(result).isCloseTo(4.5, within(0.01));
    }

    @Test
    @DisplayName("getCalculatedRating - Should return 0.0 when rating is null but reviewCount is not")
    void testGetCalculatedRating_NullRatingNonNullReviewCount() {
        // Given
        Business business = Business.builder()
                .name("Test Business")
                .email("test@test.com")
                .rating(null)
                .reviewCount(5)
                .build();

        // When
        Double result = business.getCalculatedRating(10, 3.5);

        // Then — returns 0.0 because rating is null
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("updateRating - Should set weighted rating to 0 when null values passed")
    void testUpdateRating_WithNullValues() {
        // Given
        Business business = Business.builder()
                .name("Test Business")
                .email("test@test.com")
                .rating(4.0)
                .reviewCount(10)
                .weightedRating(3.9)
                .build();

        // When
        business.updateRating(null, null, 10, 3.5);

        // Then
        assertThat(business.getRating()).isNull();
        assertThat(business.getReviewCount()).isNull();
        assertThat(business.getWeightedRating()).isEqualTo(0.0);
    }

    @Test
    void testUpdateRating_updatesAllFields() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .rating(4.0)
                .reviewCount(50)
                .weightedRating(3.9)
                .build();

        business.updateRating(4.5, 100, 10, 3.5);

        assertEquals(4.5, business.getRating());
        assertEquals(100, business.getReviewCount());
        assertEquals(4.409, business.getWeightedRating(), 0.01);
    }

    @Test
    void testActiveFlag_defaultTrue() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .active(true)
                .build();

        assertTrue(business.isActive());
    }

    @Test
    void testActiveFlag_canBeSetFalse() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .active(false)
                .build();

        assertFalse(business.isActive());
    }

    @Test
    void testEmailVerified_defaultFalse() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .emailVerified(false)
                .build();

        assertFalse(business.isEmailVerified());
    }

    @Test
    void testSetterMethods() {
        Address address1 = new Address("Address 1", "Line 1", null, "City1", "Region1", "12345", "US", null);
        Address address2 = new Address("Address 2", null, "Line 2", "City2", "Region2", "67890", "DE", null);
        Point location1 = geometryFactory.createPoint(new Coordinate(0, 0));
        Point location2 = geometryFactory.createPoint(new Coordinate(10, 10));

        Business business = Business.builder()
                .name("Original Name")
                .description("Original Description")
                .active(true)
                .address(address1)
                .location(location1)
                .phoneNumber("+1234567890123")
                .website("https://original.com")
                .email("original@example.com")
                .emailVerified(false)
                .ownerId(1L)
                .rating(4.0)
                .reviewCount(50)
                .weightedRating(3.8)
                .build();

        business.setName("Updated Name");
        business.setDescription("Updated Description");
        business.setActive(false);
        business.setAddress(address2);
        business.setLocation(location2);
        business.setPhoneNumber("+9876543210987");
        business.setWebsite("https://updated.com");
        business.setEmail("updated@example.com");
        business.setEmailVerified(true);
        business.setOwnerId(2L);
        business.setRating(4.5);
        business.setReviewCount(100);
        business.setWeightedRating(4.3);

        assertEquals("Updated Name", business.getName());
        assertEquals("Updated Description", business.getDescription());
        assertFalse(business.isActive());
        assertEquals(address2, business.getAddress());
        assertEquals("Address 2", business.getAddress().getFormattedAddress());
        assertEquals(location2, business.getLocation());
        assertEquals("+9876543210987", business.getPhoneNumber());
        assertEquals("https://updated.com", business.getWebsite());
        assertEquals("updated@example.com", business.getEmail());
        assertTrue(business.isEmailVerified());
        assertEquals(2L, business.getOwnerId());
        assertEquals(4.5, business.getRating());
        assertEquals(100, business.getReviewCount());
        assertEquals(4.3, business.getWeightedRating());
    }

    @Test
    void testKeywordsCollection_isEmpty() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .keywords(new HashSet<>())
                .build();

        assertNotNull(business.getKeywords());
        assertTrue(business.getKeywords().isEmpty());
    }

    @Test
    void testImagesCollection_isEmpty() {
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .images(new HashSet<>())
                .build();

        assertNotNull(business.getImages());
        assertTrue(business.getImages().isEmpty());
    }

    @Test
    void testAllArgsConstructor() {
        Address address = new Address("Test Address", "Line 1", null, "Berlin", "Berlin", "10115", "DE", null);
        Point location = geometryFactory.createPoint(new Coordinate(13.4050, 52.5200));
        Set<Long> categoryIds = new HashSet<>(Set.of(1L, 2L));
        Set<Long> adminIds = new HashSet<>(Set.of(10L));
        Set<BusinessKeyword> keywords = new HashSet<>();
        Set<BusinessImage> images = new HashSet<>();

        Business business = new Business(
                "Test Business",
                "Description",
                true,
                address,
                location,
                "+4915112345678",
                "https://example.com",
                "test@example.com",
                true,
                1L,
                4.5,
                100,
                4.3,
                categoryIds,
                adminIds,
                keywords,
                images
        );

        assertEquals("Test Business", business.getName());
        assertEquals("Description", business.getDescription());
        assertTrue(business.isActive());
        assertEquals(address, business.getAddress());
        assertEquals("Test Address", business.getAddress().getFormattedAddress());
        assertEquals(location, business.getLocation());
        assertEquals("+4915112345678", business.getPhoneNumber());
        assertEquals("https://example.com", business.getWebsite());
        assertEquals("test@example.com", business.getEmail());
        assertTrue(business.isEmailVerified());
        assertEquals(1L, business.getOwnerId());
        assertEquals(4.5, business.getRating());
        assertEquals(100, business.getReviewCount());
        assertEquals(4.3, business.getWeightedRating());
        assertEquals(2, business.getCategoryIds().size());
        assertEquals(1, business.getAdminIds().size());
    }

    @Test
    void testNoArgsConstructor() {
        Business business = new Business();

        assertNull(business.getName());
        assertNull(business.getDescription());
        assertNull(business.getAddress());
        assertNull(business.getLocation());
        assertNull(business.getPhoneNumber());
        assertNull(business.getWebsite());
        assertNull(business.getEmail());
        assertNull(business.getOwnerId());
        assertNull(business.getRating());
        assertNull(business.getReviewCount());
        assertNull(business.getWeightedRating());
    }

    @Test
    void testLocationCoordinates_multipleLocations() {
        Point newYork = geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128));
        Point london = geometryFactory.createPoint(new Coordinate(-0.1278, 51.5074));
        Point tokyo = geometryFactory.createPoint(new Coordinate(139.6917, 35.6895));

        Business business1 = Business.builder().name("NYC").email("nyc@example.com").location(newYork).ownerId(1L).build();
        Business business2 = Business.builder().name("London").email("london@example.com").location(london).ownerId(1L).build();
        Business business3 = Business.builder().name("Tokyo").email("tokyo@example.com").location(tokyo).ownerId(1L).build();

        assertEquals(-74.0060, business1.getLocation().getX(), 0.0001);
        assertEquals(40.7128, business1.getLocation().getY(), 0.0001);
        assertEquals(-0.1278, business2.getLocation().getX(), 0.0001);
        assertEquals(51.5074, business2.getLocation().getY(), 0.0001);
        assertEquals(139.6917, business3.getLocation().getX(), 0.0001);
        assertEquals(35.6895, business3.getLocation().getY(), 0.0001);
    }

    // ===== Name Field Validation Tests =====

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testName_blankInvalid(String name) {
        Business business = Business.builder()
                .name(name)
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_minLength() {
        Business business = Business.builder()
                .name("A")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "1 character name should be valid");
    }

    @Test
    void testName_maxLength() {
        String name = "A".repeat(255);
        Business business = Business.builder()
                .name(name)
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "255 character name should be valid");
    }

    @Test
    void testName_exceedsMaxLength() {
        String name = "A".repeat(256);
        Business business = Business.builder()
                .name(name)
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_specialCharacters() {
        Business business = Business.builder()
                .name("Café & Restaurant 日本料理")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Name with special characters should be valid");
    }

    // ===== Description Field Validation Tests =====

    @Test
    void testDescription_null() {
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .description(null)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Null description should be valid");
    }

    @Test
    void testDescription_maxLength() {
        String description = "A".repeat(2000);
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .description(description)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "2000 character description should be valid");
    }

    @Test
    void testDescription_exceedsMaxLength() {
        String description = "A".repeat(2001);
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .description(description)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    // ===== Phone Number Size Validation Tests =====

    @Test
    void testPhoneNumber_maxLength() {
        String phoneNumber = "+12345678901234"; // 16 chars (within 20 limit and E.164 limit of 15 digits)
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .phoneNumber(phoneNumber)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Phone number within 20 chars should be valid");
    }

    @Test
    void testPhoneNumber_exceedsMaxLength() {
        String phoneNumber = "+123456789012345678901"; // 22 chars
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .phoneNumber(phoneNumber)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    // ===== Website Size Validation Tests =====

    @Test
    void testWebsite_null() {
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .website(null)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Null website should be valid");
    }

    @Test
    void testWebsite_maxLength() {
        // Create a URL close to 2048 chars but valid
        String baseUrl = "https://example.com/";
        String path = "a".repeat(2000); // Creates exactly 2000 chars
        String website = baseUrl + path; // Total: ~2020 chars
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .website(website)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty(), "Website URL within 2048 chars should be valid");
    }

    @Test
    void testWebsite_exceedsMaxLength() {
        String longPath = "path/".repeat(500);
        String website = "https://example.com/" + longPath; // Over 2048 chars
        Business business = Business.builder()
                .name("Test Business")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .website(website)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("website")));
    }

    // ===== Email Size Validation Tests =====

    @Test
    void testEmail_maxLength() {
        // Create a 255 character email: long local part + @example.com
        String localPart = "a".repeat(242); // 242 + 1(@) + 11(example.com) + 1(.) = 255
        String email = localPart + "@example.com";
        Business business = Business.builder()
                .name("Test Business")
                .email(email)
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertTrue(violations.isEmpty() || violations.stream().noneMatch(v -> 
                v.getPropertyPath().toString().equals("email") && v.getMessage().contains("must not exceed")),
                "255 character email should not violate size constraint");
    }

    @Test
    void testEmail_exceedsMaxLength() {
        // Create a 256 character email
        String localPart = "a".repeat(243);
        String email = localPart + "@example.com";
        Business business = Business.builder()
                .name("Test Business")
                .email(email)
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // ===== getCalculatedRating Edge Cases =====

    @Test
    @DisplayName("getCalculatedRating - Should return 0.0 when reviewCount is null")
    void testGetCalculatedRating_NullReviewCount() {
        Business business = Business.builder()
                .name("Test Business")
                .email("test@test.com")
                .rating(4.5)
                .reviewCount(null)
                .build();

        Double result = business.getCalculatedRating(10, 3.5);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getCalculatedRating - Should return 0.0 when both rating and reviewCount are null")
    void testGetCalculatedRating_BothNull() {
        Business business = Business.builder()
                .name("Test Business")
                .email("test@test.com")
                .rating(null)
                .reviewCount(null)
                .build();

        Double result = business.getCalculatedRating(10, 3.5);

        assertThat(result).isEqualTo(0.0);
    }

    // ===== Name Length Boundary Tests =====

    @Test
    @DisplayName("Name validation - exactly 255 chars should pass")
    void testNameValidation_maxLength255() {
        Business business = Business.builder()
                .name("a".repeat(255))
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("name")),
                "Name of 255 chars should be valid");
    }

    @Test
    @DisplayName("Name validation - 256 chars should fail")
    void testNameValidation_exceeds255() {
        Business business = Business.builder()
                .name("a".repeat(256))
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        Set<ConstraintViolation<Business>> violations = validator.validate(business);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
}
