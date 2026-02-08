package eu.dec21.appointme.common.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===== Constructor Tests =====

    @Test
    void testNoArgsConstructor() {
        Address address = new Address();

        assertNull(address.getFormattedAddress());
        assertNull(address.getLine1());
        assertNull(address.getLine2());
        assertNull(address.getCity());
        assertNull(address.getRegion());
        assertNull(address.getPostalCode());
        assertNull(address.getCountryCode());
        assertNull(address.getPlaceId());
    }

    @Test
    void testAllArgsConstructor() {
        Address address = new Address(
                "123 Main St, New York, NY 10001, US",
                "123 Main Street",
                "Apt 4B",
                "New York",
                "NY",
                "10001",
                "US",
                "ChIJOwg_06VPwokRYv534QaPC8g"
        );

        assertEquals("123 Main St, New York, NY 10001, US", address.getFormattedAddress());
        assertEquals("123 Main Street", address.getLine1());
        assertEquals("Apt 4B", address.getLine2());
        assertEquals("New York", address.getCity());
        assertEquals("NY", address.getRegion());
        assertEquals("10001", address.getPostalCode());
        assertEquals("US", address.getCountryCode());
        assertEquals("ChIJOwg_06VPwokRYv534QaPC8g", address.getPlaceId());
    }

    // ===== Getter/Setter Tests =====

    @Test
    void testSettersAndGetters() {
        Address address = new Address();

        address.setFormattedAddress("456 Oak Ave, Los Angeles, CA 90001, US");
        address.setLine1("456 Oak Avenue");
        address.setLine2("Suite 200");
        address.setCity("Los Angeles");
        address.setRegion("CA");
        address.setPostalCode("90001");
        address.setCountryCode("US");
        address.setPlaceId("ChIJE9on3F3HwoAR9AhGJW_fL-I");

        assertEquals("456 Oak Ave, Los Angeles, CA 90001, US", address.getFormattedAddress());
        assertEquals("456 Oak Avenue", address.getLine1());
        assertEquals("Suite 200", address.getLine2());
        assertEquals("Los Angeles", address.getCity());
        assertEquals("CA", address.getRegion());
        assertEquals("90001", address.getPostalCode());
        assertEquals("US", address.getCountryCode());
        assertEquals("ChIJE9on3F3HwoAR9AhGJW_fL-I", address.getPlaceId());
    }

    // ===== Formatted Address Validation Tests =====

    @Test
    void testFormattedAddress_withMaxLength() {
        String maxLengthFormatted = "A".repeat(512);
        Address address = new Address();
        address.setFormattedAddress(maxLengthFormatted);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFormattedAddress_exceedsMaxLength() {
        String tooLongFormatted = "A".repeat(513);
        Address address = new Address();
        address.setFormattedAddress(tooLongFormatted);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("formattedAddress") &&
                        v.getMessage().contains("must not exceed 512 characters")));
    }

    @Test
    void testFormattedAddress_nullIsValid() {
        Address address = new Address();
        address.setFormattedAddress(null);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    // ===== Line1 Validation Tests =====

    @Test
    void testLine1_withValidLength() {
        Address address = new Address();
        address.setLine1("123 Main Street");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLine1_withMaxLength() {
        String maxLengthLine1 = "L".repeat(255);
        Address address = new Address();
        address.setLine1(maxLengthLine1);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLine1_exceedsMaxLength() {
        String tooLongLine1 = "L".repeat(256);
        Address address = new Address();
        address.setLine1(tooLongLine1);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("line1")));
    }

    // ===== Line2 Validation Tests =====

    @Test
    void testLine2_withMaxLength() {
        String maxLengthLine2 = "L".repeat(255);
        Address address = new Address();
        address.setLine2(maxLengthLine2);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLine2_exceedsMaxLength() {
        String tooLongLine2 = "L".repeat(256);
        Address address = new Address();
        address.setLine2(tooLongLine2);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("line2")));
    }

    @Test
    void testLine2_nullIsValid() {
        Address address = new Address();
        address.setLine2(null);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    // ===== City Validation Tests =====

    @Test
    void testCity_withValidLength() {
        Address address = new Address();
        address.setCity("New York");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCity_withMaxLength() {
        String maxLengthCity = "C".repeat(128);
        Address address = new Address();
        address.setCity(maxLengthCity);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCity_exceedsMaxLength() {
        String tooLongCity = "C".repeat(129);
        Address address = new Address();
        address.setCity(tooLongCity);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("city")));
    }

    // ===== Region Validation Tests =====

    @Test
    void testRegion_withValidLength() {
        Address address = new Address();
        address.setRegion("California");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRegion_withMaxLength() {
        String maxLengthRegion = "R".repeat(128);
        Address address = new Address();
        address.setRegion(maxLengthRegion);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRegion_exceedsMaxLength() {
        String tooLongRegion = "R".repeat(129);
        Address address = new Address();
        address.setRegion(tooLongRegion);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("region")));
    }

    // ===== Postal Code Validation Tests =====

    @Test
    void testPostalCode_withValidLength() {
        Address address = new Address();
        address.setPostalCode("10001");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPostalCode_withMaxLength() {
        String maxLengthPostal = "P".repeat(32);
        Address address = new Address();
        address.setPostalCode(maxLengthPostal);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPostalCode_exceedsMaxLength() {
        String tooLongPostal = "P".repeat(33);
        Address address = new Address();
        address.setPostalCode(tooLongPostal);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("postalCode")));
    }

    // ===== Country Code Validation Tests =====

    @ParameterizedTest
    @ValueSource(strings = {"US", "GB", "DE", "FR", "JP", "CA", "AU", "IN", "BR", "MX"})
    void testCountryCode_withValidCodes(String countryCode) {
        Address address = new Address();
        address.setCountryCode(countryCode);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USA", "U", "us", "U1", "1A", "ABC", ""})
    void testCountryCode_withInvalidCodes(String countryCode) {
        Address address = new Address();
        address.setCountryCode(countryCode);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("countryCode")));
    }

    @Test
    void testCountryCode_nullIsValid() {
        Address address = new Address();
        address.setCountryCode(null);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    // ===== Place ID Validation Tests =====

    @Test
    void testPlaceId_withValidGooglePlaceId() {
        Address address = new Address();
        address.setPlaceId("ChIJOwg_06VPwokRYv534QaPC8g");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPlaceId_withMaxLength() {
        String maxLengthPlaceId = "P".repeat(128);
        Address address = new Address();
        address.setPlaceId(maxLengthPlaceId);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPlaceId_exceedsMaxLength() {
        String tooLongPlaceId = "P".repeat(129);
        Address address = new Address();
        address.setPlaceId(tooLongPlaceId);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("placeId")));
    }

    @Test
    void testPlaceId_nullIsValid() {
        Address address = new Address();
        address.setPlaceId(null);

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    // ===== Complete Address Tests =====

    @Test
    void testCompleteUSAddress() {
        Address address = new Address(
                "1600 Pennsylvania Avenue NW, Washington, DC 20500, US",
                "1600 Pennsylvania Avenue NW",
                null,
                "Washington",
                "DC",
                "20500",
                "US",
                "ChIJGVtI4by3t4kRr51d_Qm_x58"
        );

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCompleteGermanAddress() {
        Address address = new Address(
                "Brandenburger Tor, 10117 Berlin, Germany",
                "Pariser Platz",
                null,
                "Berlin",
                "Berlin",
                "10117",
                "DE",
                "ChIJsU7ZkhpOqEcRoDrWe_I9JgQ"
        );

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCompleteUKAddress() {
        Address address = new Address(
                "10 Downing Street, London SW1A 2AA, UK",
                "10 Downing Street",
                null,
                "London",
                "England",
                "SW1A 2AA",
                "GB",
                "ChIJJXhSa3MEdkgRnA5ZW8J1FXg"
        );

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMinimalAddress() {
        Address address = new Address();
        address.setCity("Berlin");
        address.setCountryCode("DE");

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMultipleViolations() {
        Address address = new Address();
        address.setLine1("L".repeat(256)); // Too long
        address.setCity("C".repeat(129)); // Too long
        address.setCountryCode("USA"); // Invalid - both size (3 chars) and pattern violations

        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.isEmpty());
        assertEquals(4, violations.size()); // line1, city, countryCode size, countryCode pattern

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("line1")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("city")));
        // CountryCode has 2 violations: size and pattern
        assertEquals(2, violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("countryCode"))
                .count());
    }
}
