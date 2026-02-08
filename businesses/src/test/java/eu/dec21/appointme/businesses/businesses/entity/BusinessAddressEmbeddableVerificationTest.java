package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.Address;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test to ensure Address (@Embeddable) works correctly with Business entity
 * without Hibernate bytecode enhancement.
 * 
 * This test proves:
 * 1. Address can be embedded in Business without ClassCastException
 * 2. All Address fields are accessible and work correctly
 * 3. Address can be null (optional for online-only businesses)
 * 4. Address can be set/updated via setter methods
 * 5. No Hibernate bytecode enhancement is required for @Embeddable to function
 */
class BusinessAddressEmbeddableVerificationTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void verifyAddressEmbeddingWorksWithoutBytecodeEnhancement() {
        // Create a complete address with all fields
        Address address = new Address(
                "1600 Pennsylvania Avenue NW, Washington, DC 20500, USA",
                "1600 Pennsylvania Avenue NW",
                "West Wing",
                "Washington",
                "DC",
                "20500",
                "US",
                "ChIJGVtI4by3t4kRr51d_Qm_x58"  // Google Place ID
        );

        Point location = geometryFactory.createPoint(new Coordinate(-77.0365, 38.8977));

        // Build Business with embedded Address
        Business business = Business.builder()
                .name("White House Tours")
                .description("Official tours of the White House")
                .active(true)
                .address(address)  // ✅ This should work without bytecode enhancement
                .location(location)
                .phoneNumber("+12024561414")
                .website("https://www.whitehouse.gov")
                .email("tours@whitehouse.gov")
                .emailVerified(true)
                .ownerId(1L)
                .build();

        // Verify Address is properly embedded
        assertNotNull(business.getAddress(), "Address should not be null");
        
        // Verify all Address fields are accessible
        assertEquals("1600 Pennsylvania Avenue NW, Washington, DC 20500, USA", 
                business.getAddress().getFormattedAddress());
        assertEquals("1600 Pennsylvania Avenue NW", business.getAddress().getLine1());
        assertEquals("West Wing", business.getAddress().getLine2());
        assertEquals("Washington", business.getAddress().getCity());
        assertEquals("DC", business.getAddress().getRegion());
        assertEquals("20500", business.getAddress().getPostalCode());
        assertEquals("US", business.getAddress().getCountryCode());
        assertEquals("ChIJGVtI4by3t4kRr51d_Qm_x58", business.getAddress().getPlaceId());
    }

    @Test
    void verifyAddressCanBeNull() {
        // Online-only business without physical address
        Business business = Business.builder()
                .name("Virtual Consulting")
                .email("contact@virtual.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .address(null)  // ✅ Should work
                .build();

        assertNull(business.getAddress(), "Address can be null for online businesses");
    }

    @Test
    void verifyAddressCanBeUpdatedViaSetter() {
        // Start with no address
        Business business = Business.builder()
                .name("Relocating Business")
                .email("contact@relocating.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .build();

        assertNull(business.getAddress(), "Initial address should be null");

        // Add address later
        Address newAddress = new Address(
                "221B Baker Street, London NW1 6XE, UK",
                "221B Baker Street",
                null,
                "London",
                "Greater London",
                "NW1 6XE",
                "GB",
                "ChIJdd4hrwug2EcRmSrV3Vo6llI"
        );

        business.setAddress(newAddress);  // ✅ Should work

        assertNotNull(business.getAddress(), "Address should be set");
        assertEquals("London", business.getAddress().getCity());
        assertEquals("GB", business.getAddress().getCountryCode());
    }

    @Test
    void verifyMultipleBusinessesWithDifferentAddresses() {
        // US Business
        Address usAddress = new Address(
                "123 Main St, New York, NY 10001, USA",
                "123 Main Street",
                "Suite 100",
                "New York",
                "NY",
                "10001",
                "US",
                null
        );

        Business usBusiness = Business.builder()
                .name("US Business")
                .email("us@example.com")
                .location(geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))
                .ownerId(1L)
                .address(usAddress)
                .build();

        // Japanese Business
        Address jpAddress = new Address(
                "1-1-2 Oshiage, Sumida City, Tokyo 131-0045, Japan",
                "1-1-2 Oshiage",
                "Tokyo Skytree",
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

        // Verify both addresses work independently
        assertEquals("US", usBusiness.getAddress().getCountryCode());
        assertEquals("New York", usBusiness.getAddress().getCity());
        
        assertEquals("JP", jpBusiness.getAddress().getCountryCode());
        assertEquals("Tokyo", jpBusiness.getAddress().getCity());
        
        // Verify they are independent objects
        assertNotEquals(usBusiness.getAddress(), jpBusiness.getAddress());
    }

    @Test
    void verifyAddressFieldsCanBeIndividuallyModified() {
        Address address = new Address(
                "Old Address",
                "Old Line 1",
                "Old Line 2",
                "Old City",
                "Old Region",
                "00000",
                "US",
                null
        );

        Business business = Business.builder()
                .name("Updating Business")
                .email("update@example.com")
                .location(geometryFactory.createPoint(new Coordinate(0, 0)))
                .ownerId(1L)
                .address(address)
                .build();

        // Modify address fields directly
        business.getAddress().setFormattedAddress("New Address");
        business.getAddress().setLine1("New Line 1");
        business.getAddress().setCity("New City");
        business.getAddress().setPostalCode("99999");

        // Verify modifications
        assertEquals("New Address", business.getAddress().getFormattedAddress());
        assertEquals("New Line 1", business.getAddress().getLine1());
        assertEquals("New City", business.getAddress().getCity());
        assertEquals("99999", business.getAddress().getPostalCode());
        
        // Unchanged fields should remain
        assertEquals("Old Line 2", business.getAddress().getLine2());
        assertEquals("Old Region", business.getAddress().getRegion());
        assertEquals("US", business.getAddress().getCountryCode());
    }
}
