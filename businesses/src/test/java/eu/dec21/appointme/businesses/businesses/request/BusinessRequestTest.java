package eu.dec21.appointme.businesses.businesses.request;

import eu.dec21.appointme.common.entity.Address;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for BusinessRequest record.
 * Tests record properties, validation constraints, immutability, and edge cases.
 */
@DisplayName("BusinessRequest Tests")
class BusinessRequestTest {

    private static Validator validator;
    private static GeometryFactory geometryFactory;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    @Test
    @DisplayName("Should create BusinessRequest with all fields")
    void testCreateWithAllFields() {
        // Given
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        // When
        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                "A test business description",
                address,
                location,
                "+491234567890",
                "https://example.com",
                "test@example.com"
        );

        // Then
        assertThat(request).isNotNull();
        assertThat(request.id()).isEqualTo(1L);
        assertThat(request.name()).isEqualTo("Test Business");
        assertThat(request.description()).isEqualTo("A test business description");
        assertThat(request.address()).isEqualTo(address);
        assertThat(request.location()).isEqualTo(location);
        assertThat(request.phoneNumber()).isEqualTo("+491234567890");
        assertThat(request.website()).isEqualTo("https://example.com");
        assertThat(request.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should create BusinessRequest with minimal required fields")
    void testCreateWithMinimalFields() {
        // When
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

        // Then
        assertThat(request).isNotNull();
        assertThat(request.id()).isNull();
        assertThat(request.name()).isEqualTo("Minimal Business");
        assertThat(request.description()).isNull();
        assertThat(request.address()).isNull();
        assertThat(request.location()).isNull();
        assertThat(request.phoneNumber()).isNull();
        assertThat(request.website()).isNull();
        assertThat(request.email()).isNull();
    }

    @Test
    @DisplayName("Should pass validation with valid name")
    void testValidation_ValidName() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "Valid Business Name",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void testValidation_NullName() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                null,
                "Description",
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<BusinessRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Name cannot be blank");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void testValidation_BlankName() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "   ",
                "Description",
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<BusinessRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("Should fail validation when name is empty string")
    void testValidation_EmptyName() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "",
                "Description",
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<BusinessRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("Should allow single character name")
    void testValidation_SingleCharacterName() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "X",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should demonstrate record immutability")
    void testRecordImmutability() {
        // Given
        Address address = createAddress();
        BusinessRequest request = new BusinessRequest(
                1L,
                "Original Name",
                "Original Description",
                address,
                null,
                null,
                null,
                null
        );

        // When - attempt to get fields (records have no setters)
        Long id = request.id();
        String name = request.name();
        Address retrievedAddress = request.address();

        // Then - values are immutable through the record
        assertThat(id).isEqualTo(1L);
        assertThat(name).isEqualTo("Original Name");
        assertThat(retrievedAddress).isEqualTo(address);

        // Records don't have setters - this is a compilation feature
        // request.name = "New Name"; // Would not compile
    }

    @Test
    @DisplayName("Should implement equals() correctly for identical records")
    void testEquals_IdenticalRecords() {
        // Given
        Address address = createAddress();
        Point location = createPoint(13.4050, 52.5200);

        BusinessRequest request1 = new BusinessRequest(
                1L,
                "Test Business",
                "Description",
                address,
                location,
                "+491234567890",
                "https://example.com",
                "test@example.com"
        );

        BusinessRequest request2 = new BusinessRequest(
                1L,
                "Test Business",
                "Description",
                address,
                location,
                "+491234567890",
                "https://example.com",
                "test@example.com"
        );

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Should implement equals() correctly for different records")
    void testEquals_DifferentRecords() {
        // Given
        BusinessRequest request1 = new BusinessRequest(
                1L,
                "Business One",
                null,
                null,
                null,
                null,
                null,
                null
        );

        BusinessRequest request2 = new BusinessRequest(
                2L,
                "Business Two",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertThat(request1).isNotEqualTo(request2);
        assertThat(request1.hashCode()).isNotEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Should implement equals() correctly when only name differs")
    void testEquals_DifferentNames() {
        // Given
        BusinessRequest request1 = new BusinessRequest(1L, "Name A", null, null, null, null, null, null);
        BusinessRequest request2 = new BusinessRequest(1L, "Name B", null, null, null, null, null, null);

        // Then
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    @DisplayName("Should implement equals() correctly for null fields")
    void testEquals_NullFields() {
        // Given
        BusinessRequest request1 = new BusinessRequest(null, "Name", null, null, null, null, null, null);
        BusinessRequest request2 = new BusinessRequest(null, "Name", null, null, null, null, null, null);

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString() correctly")
    void testToString() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                "Description",
                null,
                null,
                "+491234567890",
                "https://example.com",
                "test@example.com"
        );

        // When
        String toString = request.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("BusinessRequest");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Test Business");
        assertThat(toString).contains("description=Description");
        assertThat(toString).contains("phoneNumber=+491234567890");
        assertThat(toString).contains("website=https://example.com");
        assertThat(toString).contains("email=test@example.com");
    }

    @Test
    @DisplayName("Should handle very long name")
    void testLongName() {
        // Given
        String longName = "a".repeat(1000);

        // When
        BusinessRequest request = new BusinessRequest(
                1L,
                longName,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertThat(request.name()).hasSize(1000);
        
        // Validation should pass (no max length constraint on name)
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        // Given
        String specialName = "Business & Co. <Test> @#$%";

        // When
        BusinessRequest request = new BusinessRequest(
                1L,
                specialName,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertThat(request.name()).isEqualTo(specialName);
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle Unicode characters in name")
    void testUnicodeCharactersInName() {
        // Given
        String unicodeName = "Café München ☕ 日本語";

        // When
        BusinessRequest request = new BusinessRequest(
                1L,
                unicodeName,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertThat(request.name()).isEqualTo(unicodeName);
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long description")
    void testLongDescription() {
        // Given
        String longDescription = "x".repeat(5000);

        // When
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

        // Then
        assertThat(request.description()).hasSize(5000);
    }

    @Test
    @DisplayName("Should handle empty string for optional fields")
    void testEmptyStringsForOptionalFields() {
        // When
        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                "",
                null,
                null,
                "",
                "",
                ""
        );

        // Then
        assertThat(request.description()).isEmpty();
        assertThat(request.phoneNumber()).isEmpty();
        assertThat(request.website()).isEmpty();
        assertThat(request.email()).isEmpty();
    }

    @Test
    @DisplayName("Should handle different Point coordinates")
    void testDifferentCoordinates() {
        // Given
        Point berlinPoint = createPoint(13.4050, 52.5200);
        Point tokyoPoint = createPoint(139.6917, 35.6895);
        Point sydneyPoint = createPoint(151.2093, -33.8688); // Negative latitude

        // When/Then - Berlin
        BusinessRequest request1 = new BusinessRequest(1L, "Berlin Business", null, null, berlinPoint, null, null, null);
        assertThat(request1.location().getX()).isEqualTo(13.4050);
        assertThat(request1.location().getY()).isEqualTo(52.5200);

        // Tokyo
        BusinessRequest request2 = new BusinessRequest(2L, "Tokyo Business", null, null, tokyoPoint, null, null, null);
        assertThat(request2.location().getX()).isEqualTo(139.6917);
        assertThat(request2.location().getY()).isEqualTo(35.6895);

        // Sydney (negative latitude)
        BusinessRequest request3 = new BusinessRequest(3L, "Sydney Business", null, null, sydneyPoint, null, null, null);
        assertThat(request3.location().getX()).isEqualTo(151.2093);
        assertThat(request3.location().getY()).isEqualTo(-33.8688);
    }

    @Test
    @DisplayName("Should handle Address with all fields")
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

        // Then
        assertThat(request.address()).isNotNull();
        assertThat(request.address().getFormattedAddress()).isEqualTo("123 Test Street, Berlin, 10115, Germany");
        assertThat(request.address().getLine1()).isEqualTo("123 Test Street");
        assertThat(request.address().getLine2()).isEqualTo("Apartment 4B");
        assertThat(request.address().getCity()).isEqualTo("Berlin");
        assertThat(request.address().getRegion()).isEqualTo("Berlin");
        assertThat(request.address().getPostalCode()).isEqualTo("10115");
        assertThat(request.address().getCountryCode()).isEqualTo("DE");
        assertThat(request.address().getPlaceId()).isEqualTo("ChIJAVkDPzdOqEcRcDteW0YgIQQ");
    }

    @Test
    @DisplayName("Should handle Address with minimal fields")
    void testAddressWithMinimalFields() {
        // Given
        Address address = new Address();
        address.setCity("Berlin");
        address.setCountryCode("DE");

        // When
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

        // Then
        assertThat(request.address()).isNotNull();
        assertThat(request.address().getCity()).isEqualTo("Berlin");
        assertThat(request.address().getCountryCode()).isEqualTo("DE");
    }

    @Test
    @DisplayName("Should handle phone numbers in various formats")
    void testPhoneNumberFormats() {
        // Various international phone number formats
        String[] phoneNumbers = {
                "+491234567890",
                "+1-555-123-4567",
                "+44 20 7946 0958",
                "(555) 123-4567",
                "555.123.4567"
        };

        for (String phoneNumber : phoneNumbers) {
            BusinessRequest request = new BusinessRequest(
                    1L,
                    "Test Business",
                    null,
                    null,
                    null,
                    phoneNumber,
                    null,
                    null
            );

            assertThat(request.phoneNumber()).isEqualTo(phoneNumber);
        }
    }

    @Test
    @DisplayName("Should handle website URLs in various formats")
    void testWebsiteURLFormats() {
        // Various URL formats
        String[] websites = {
                "https://example.com",
                "http://example.com",
                "https://www.example.com/path",
                "https://subdomain.example.com",
                "https://example.com:8080/path?query=value",
                "https://example.co.uk"
        };

        for (String website : websites) {
            BusinessRequest request = new BusinessRequest(
                    1L,
                    "Test Business",
                    null,
                    null,
                    null,
                    null,
                    website,
                    null
            );

            assertThat(request.website()).isEqualTo(website);
        }
    }

    @Test
    @DisplayName("Should handle email addresses in various formats")
    void testEmailFormats() {
        // Various email formats
        String[] emails = {
                "test@example.com",
                "user+tag@example.co.uk",
                "firstname.lastname@example.com",
                "email@subdomain.example.com",
                "1234567890@example.com"
        };

        for (String email : emails) {
            BusinessRequest request = new BusinessRequest(
                    1L,
                    "Test Business",
                    null,
                    null,
                    null,
                    null,
                    null,
                    email
            );

            assertThat(request.email()).isEqualTo(email);
        }
    }

    @Test
    @DisplayName("Should handle ID with different values")
    void testDifferentIdValues() {
        // Test various ID values
        Long[] ids = {null, 0L, 1L, 999999L, Long.MAX_VALUE};

        for (Long id : ids) {
            BusinessRequest request = new BusinessRequest(
                    id,
                    "Test Business",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            assertThat(request.id()).isEqualTo(id);
        }
    }

    @Test
    @DisplayName("Should be different instances even with same values")
    void testInstanceIdentity() {
        // Given
        BusinessRequest request1 = new BusinessRequest(1L, "Test", null, null, null, null, null, null);
        BusinessRequest request2 = new BusinessRequest(1L, "Test", null, null, null, null, null, null);

        // Then - same values but different instances
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotSameAs(request2);
    }

    @Test
    @DisplayName("Should handle newlines and tabs in description")
    void testSpecialWhitespaceInDescription() {
        // Given
        String descriptionWithWhitespace = "Line 1\nLine 2\tTabbed\r\nWindows newline";

        // When
        BusinessRequest request = new BusinessRequest(
                1L,
                "Test Business",
                descriptionWithWhitespace,
                null,
                null,
                null,
                null,
                null
        );

        // Then
        assertThat(request.description()).isEqualTo(descriptionWithWhitespace);
    }

    @Test
    @DisplayName("Should validate with name containing only spaces is invalid")
    void testNameWithOnlySpaces() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "     ",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should validate with name containing leading/trailing spaces")
    void testNameWithLeadingTrailingSpaces() {
        // Given
        BusinessRequest request = new BusinessRequest(
                1L,
                "  Valid Name  ",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<BusinessRequest>> violations = validator.validate(request);

        // Then - @NotBlank allows leading/trailing spaces as long as there's non-whitespace content
        assertThat(violations).isEmpty();
        assertThat(request.name()).isEqualTo("  Valid Name  ");
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
