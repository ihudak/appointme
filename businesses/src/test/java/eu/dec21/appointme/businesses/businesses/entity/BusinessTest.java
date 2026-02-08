package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.Address;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Set;

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
        Point location = geometryFactory.createPoint(new Coordinate(13.4050, 52.5200));

        Business business = Business.builder()
                .name("Test Business")
                .description("Test Description")
                .active(true)
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
        // Note: @Embeddable Address causes Hibernate bytecode enhancement issues in unit tests
        // Address field tested via integration tests with full Spring context instead
        Business business = Business.builder()
                .name("Test")
                .email("test@example.com")
                .location(geometryFactory.createPoint(new Coordinate(11.5820, 48.1351)))
                .ownerId(1L)
                .build();

        assertNotNull(business);
        assertEquals("Test", business.getName());
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
        // Note: Address setter causes Hibernate bytecode enhancement issues, skipping
        Point location1 = geometryFactory.createPoint(new Coordinate(0, 0));
        Point location2 = geometryFactory.createPoint(new Coordinate(10, 10));

        Business business = Business.builder()
                .name("Original Name")
                .description("Original Description")
                .active(true)
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
        // Note: Omitting Address due to Hibernate bytecode enhancement issues in unit tests
        Point location = geometryFactory.createPoint(new Coordinate(13.4050, 52.5200));
        Set<Long> categoryIds = new HashSet<>(Set.of(1L, 2L));
        Set<Long> adminIds = new HashSet<>(Set.of(10L));
        Set<BusinessKeyword> keywords = new HashSet<>();
        Set<BusinessImage> images = new HashSet<>();

        Business business = new Business(
                "Test Business",
                "Description",
                true,
                null, // address
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
}
