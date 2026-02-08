package eu.dec21.appointme.businesses.businesses.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessImageTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .altText("Test image alt text")
                .displayOrder(1)
                .isIcon(true)
                .build();

        assertNotNull(image);
        assertEquals(business, image.getBusiness());
        assertEquals("https://example.com/image.jpg", image.getImageUrl());
        assertEquals("Test image alt text", image.getAltText());
        assertEquals(1, image.getDisplayOrder());
        assertTrue(image.getIsIcon());
    }

    @Test
    void testBuilder_withMinimalFields() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertNotNull(image);
        assertEquals(business, image.getBusiness());
        assertEquals("https://example.com/image.jpg", image.getImageUrl());
        assertNull(image.getAltText());
        assertEquals(0, image.getDisplayOrder());
        assertFalse(image.getIsIcon());
    }

    @Test
    void testNoArgsConstructor() {
        BusinessImage image = new BusinessImage();
        assertNotNull(image);
        assertNull(image.getBusiness());
        assertNull(image.getImageUrl());
        assertNull(image.getAltText());
        assertEquals(0, image.getDisplayOrder()); // Default value from field initialization
        assertFalse(image.getIsIcon()); // Default value from field initialization
    }

    @Test
    void testAllArgsConstructor() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = new BusinessImage(business, "https://example.com/image.jpg", "Alt text", 5, true);

        assertNotNull(image);
        assertEquals(business, image.getBusiness());
        assertEquals("https://example.com/image.jpg", image.getImageUrl());
        assertEquals("Alt text", image.getAltText());
        assertEquals(5, image.getDisplayOrder());
        assertTrue(image.getIsIcon());
    }

    @Test
    void testSetters() {
        Business business1 = Business.builder()
                .name("Business 1")
                .build();
        Business business2 = Business.builder()
                .name("Business 2")
                .build();

        BusinessImage image = new BusinessImage();
        image.setBusiness(business1);
        image.setImageUrl("https://example.com/image1.jpg");
        image.setAltText("Original alt text");
        image.setDisplayOrder(1);
        image.setIsIcon(false);

        assertEquals(business1, image.getBusiness());
        assertEquals("https://example.com/image1.jpg", image.getImageUrl());
        assertEquals("Original alt text", image.getAltText());
        assertEquals(1, image.getDisplayOrder());
        assertFalse(image.getIsIcon());

        // Update values
        image.setBusiness(business2);
        image.setImageUrl("https://example.com/image2.jpg");
        image.setAltText("Updated alt text");
        image.setDisplayOrder(10);
        image.setIsIcon(true);

        assertEquals(business2, image.getBusiness());
        assertEquals("https://example.com/image2.jpg", image.getImageUrl());
        assertEquals("Updated alt text", image.getAltText());
        assertEquals(10, image.getDisplayOrder());
        assertTrue(image.getIsIcon());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testImageUrl_whenBlank_shouldFailValidation(String blankUrl) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl(blankUrl)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "www.missing-protocol.com/image.jpg",
            "https://",
            "://missing-scheme.com"
    })
    void testImageUrl_whenInvalidUrl_shouldFailValidation(String invalidUrl) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl(invalidUrl)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl") &&
                        v.getMessage().contains("URL")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com/image.jpg",
            "https://cdn.example.com/images/photo.png",
            "https://storage.googleapis.com/bucket/image.webp",
            "https://s3.amazonaws.com/bucket/folder/image.gif",
            "http://localhost:8080/images/test.jpg",
            "https://example.com/path/to/image.jpg?size=large&format=webp"
    })
    void testImageUrl_whenValid_shouldPassValidation(String validUrl) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl(validUrl)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testAltText_whenNull_shouldPassValidation() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .altText(null)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testAltText_withSpecialCharacters() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String specialAltText = "Image with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?äöüßéñ中文🎉";

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .altText(specialAltText)
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertEquals(specialAltText, image.getAltText());
        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDisplayOrder_withZero() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertEquals(0, image.getDisplayOrder());
    }

    @Test
    void testDisplayOrder_withPositiveNumber() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(999)
                .isIcon(false)
                .build();

        assertEquals(999, image.getDisplayOrder());
    }

    @Test
    void testDisplayOrder_withNegativeNumber() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(-1)
                .isIcon(false)
                .build();

        assertEquals(-1, image.getDisplayOrder());
    }

    @Test
    void testIsIcon_defaultValueFalse() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = new BusinessImage();
        image.setBusiness(business);
        image.setImageUrl("https://example.com/image.jpg");
        image.setDisplayOrder(0);

        // Default value from field initialization
        assertFalse(image.getIsIcon());
    }

    @Test
    void testIsIcon_setToTrue() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(0)
                .isIcon(true)
                .build();

        assertTrue(image.getIsIcon());
    }

    @Test
    void testIsIcon_setToFalse() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertFalse(image.getIsIcon());
    }

    @Test
    void testBusinessRelationship_lazy() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertNotNull(image.getBusiness());
        assertEquals(business, image.getBusiness());
        assertEquals("Test Business", image.getBusiness().getName());
    }

    @Test
    void testBusinessRelationship_bidirectional() {
        Business business = Business.builder()
                .name("Test Business")
                .images(new java.util.LinkedHashSet<>())
                .build();

        BusinessImage image1 = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image1.jpg")
                .displayOrder(0)
                .isIcon(false)
                .build();

        BusinessImage image2 = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image2.jpg")
                .displayOrder(1)
                .isIcon(false)
                .build();

        business.getImages().add(image1);
        business.getImages().add(image2);

        assertEquals(2, business.getImages().size());
        assertTrue(business.getImages().contains(image1));
        assertTrue(business.getImages().contains(image2));
        assertEquals(business, image1.getBusiness());
        assertEquals(business, image2.getBusiness());
    }

    @Test
    void testMultipleImagesWithDifferentDisplayOrders() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image1 = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image1.jpg")
                .displayOrder(0)
                .isIcon(false)
                .build();

        BusinessImage image2 = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image2.jpg")
                .displayOrder(1)
                .isIcon(false)
                .build();

        BusinessImage image3 = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image3.jpg")
                .displayOrder(2)
                .isIcon(true)
                .build();

        assertEquals(0, image1.getDisplayOrder());
        assertEquals(1, image2.getDisplayOrder());
        assertEquals(2, image3.getDisplayOrder());
        assertFalse(image1.getIsIcon());
        assertFalse(image2.getIsIcon());
        assertTrue(image3.getIsIcon());
    }

    @Test
    void testIconImage_separateFromGalleryImages() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage iconImage = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/icon.png")
                .altText("Business icon")
                .displayOrder(0)
                .isIcon(true)
                .build();

        BusinessImage galleryImage = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/gallery.jpg")
                .altText("Gallery image")
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertTrue(iconImage.getIsIcon());
        assertFalse(galleryImage.getIsIcon());
    }

    @Test
    void testImageUrl_withLongUrl() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String longUrl = "https://example.com/very/long/path/to/image/with/many/subdirectories/and/a/very/long/filename/image-with-descriptive-name-" +
                "and-multiple-segments.jpg?param1=value1&param2=value2&param3=value3&param4=value4";

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl(longUrl)
                .displayOrder(0)
                .isIcon(false)
                .build();

        assertEquals(longUrl, image.getImageUrl());
        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);
        assertTrue(violations.isEmpty());
    }

    // ===== ImageUrl Size Validation Tests =====

    @Test
    void testImageUrl_maxLength() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        // Create a URL close to 2048 chars
        String basePath = "https://example.com/";
        String path = "a".repeat(2000); // 2000 chars
        String imageUrl = basePath + path; // ~2020 chars total

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl(imageUrl)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertTrue(violations.isEmpty(), "2048 character URL should be valid");
    }

    @Test
    void testImageUrl_exceedsMaxLength() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        // Create a URL over 2048 chars
        String basePath = "https://example.com/";
        String path = "path/".repeat(500);
        String imageUrl = basePath + path; // Over 2048 chars

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl(imageUrl)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl") &&
                        v.getMessage().contains("must not exceed")));
    }

    // ===== AltText Size Validation Tests =====

    @Test
    void testAltText_maxLength() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String altText = "A".repeat(500);

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .altText(altText)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertTrue(violations.isEmpty(), "500 character alt text should be valid");
    }

    @Test
    void testAltText_exceedsMaxLength() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String altText = "A".repeat(501);

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .altText(altText)
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("altText") &&
                        v.getMessage().contains("must not exceed")));
    }

    @Test
    void testAltText_emptyStringValid() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessImage image = BusinessImage.builder()
                .business(business)
                .imageUrl("https://example.com/image.jpg")
                .altText("")
                .displayOrder(0)
                .isIcon(false)
                .build();

        Set<ConstraintViolation<BusinessImage>> violations = validator.validate(image);

        assertTrue(violations.isEmpty(), "Empty alt text should be valid");
    }
}
