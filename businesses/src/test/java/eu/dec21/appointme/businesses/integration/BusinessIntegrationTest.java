package eu.dec21.appointme.businesses.integration;

import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.repository.BusinessRepository;
import eu.dec21.appointme.common.entity.Address;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Businesses module using Testcontainers.
 * Tests business management with PostGIS location support.
 */
@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
class BusinessIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("appme_businesses")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private BusinessRepository businessRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private void setAuditFields(Business business) {
        business.setCreatedBy(999L);
        business.setUpdatedBy(999L);
    }

    @Test
    void shouldConnectToPostgresContainer() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldCreateAndRetrieveBusinessWithLocation() {
        // Given - Create a business with PostGIS location (Prague coordinates)
        Point location = geometryFactory.createPoint(new Coordinate(14.4378, 50.0755));
        Address address = new Address();
        address.setFormattedAddress("Václavské náměstí 1, Prague");
        address.setCity("Prague");
        address.setCountryCode("CZ");

        Business business = Business.builder()
                .name("Prague Barber Shop")
                .description("Professional barbershop in Prague city center")
                .phoneNumber("+420123456789")
                .email("info@praguebarber.cz")
                .address(address)
                .location(location)
                .ownerId(100L)
                .active(true)
                .rating(4.5)
                .reviewCount(10)
                .build();
        business.setCreatedBy(999L);
        business.setUpdatedBy(999L);
        business.getCategoryIds().addAll(Set.of(1L, 2L));

        // When
        Business savedBusiness = businessRepository.save(business);

        // Then
        assertThat(savedBusiness.getId()).isNotNull();
        assertThat(savedBusiness.getName()).isEqualTo("Prague Barber Shop");
        assertThat(savedBusiness.getLocation()).isNotNull();
        assertThat(savedBusiness.getLocation().getCoordinate().getX()).isEqualTo(14.4378);
        assertThat(savedBusiness.getLocation().getCoordinate().getY()).isEqualTo(50.0755);
        assertThat(savedBusiness.getLocation().getSRID()).isEqualTo(4326);
        assertThat(savedBusiness.getCategoryIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(savedBusiness.getOwnerId()).isEqualTo(100L);
        assertThat(savedBusiness.isActive()).isTrue();
    }

    @Test
    void shouldFindActiveBusinessesOnly() {
        // Given - Create active and inactive businesses
        Point location1 = geometryFactory.createPoint(new Coordinate(14.4378, 50.0755));
        Address address1 = new Address();
        address1.setFormattedAddress("Street 1, Prague");
        
        Business activeBusiness = Business.builder()
                .name("Active Restaurant")
                .description("Open for business")
                .phoneNumber("+420111222333")
                .email("active@restaurant.cz")
                .address(address1)
                .location(location1)
                .ownerId(101L)
                .active(true)
                .build();
        setAuditFields(activeBusiness);
        activeBusiness.getCategoryIds().add(3L);

        Point location2 = geometryFactory.createPoint(new Coordinate(14.4500, 50.0800));
        Address address2 = new Address();
        address2.setFormattedAddress("Street 2, Prague");
        
        Business inactiveBusiness = Business.builder()
                .name("Closed Restaurant")
                .description("Temporarily closed")
                .phoneNumber("+420444555666")
                .email("closed@restaurant.cz")
                .address(address2)
                .location(location2)
                .ownerId(102L)
                .active(false)
                .build();
        setAuditFields(inactiveBusiness);
        inactiveBusiness.getCategoryIds().add(3L);

        businessRepository.save(activeBusiness);
        businessRepository.save(inactiveBusiness);

        // When
        var allBusinesses = businessRepository.findAll();
        var activeBusinesses = allBusinesses.stream().filter(Business::isActive).toList();

        // Then
        assertThat(activeBusinesses).hasSize(1);
        assertThat(activeBusinesses.get(0).getName()).isEqualTo("Active Restaurant");
    }

    @Test
    void shouldUpdateBusinessRating() {
        // Given
        Point location = geometryFactory.createPoint(new Coordinate(14.4378, 50.0755));
        Address address = new Address();
        address.setFormattedAddress("Cafe Street 5, Prague");
        
        Business business = Business.builder()
                .name("Rated Cafe")
                .description("Cozy cafe")
                .phoneNumber("+420777888999")
                .email("cafe@example.cz")
                .address(address)
                .location(location)
                .ownerId(103L)
                .active(true)
                .rating(0.0)
                .reviewCount(0)
                .build();
        setAuditFields(business);
        business.getCategoryIds().add(4L);

        Business savedBusiness = businessRepository.save(business);

        // When - Update rating
        savedBusiness.setRating(4.8);
        savedBusiness.setReviewCount(25);
        Business updatedBusiness = businessRepository.save(savedBusiness);

        // Then
        assertThat(updatedBusiness.getRating()).isEqualTo(4.8);
        assertThat(updatedBusiness.getReviewCount()).isEqualTo(25);
    }

    @Test
    void shouldFindBusinessesByOwner() {
        // Given - Create multiple businesses for same owner
        Point location1 = geometryFactory.createPoint(new Coordinate(14.4378, 50.0755));
        Address address1 = new Address();
        address1.setFormattedAddress("Address 1");
        
        Business business1 = Business.builder()
                .name("Owner's Business 1")
                .description("First business")
                .phoneNumber("+420111111111")
                .email("business1@owner.cz")
                .address(address1)
                .location(location1)
                .ownerId(200L)
                .active(true)
                .build();
        setAuditFields(business1);
        business1.getCategoryIds().add(1L);

        Point location2 = geometryFactory.createPoint(new Coordinate(14.4500, 50.0800));
        Address address2 = new Address();
        address2.setFormattedAddress("Address 2");
        
        Business business2 = Business.builder()
                .name("Owner's Business 2")
                .description("Second business")
                .phoneNumber("+420222222222")
                .email("business2@owner.cz")
                .address(address2)
                .location(location2)
                .ownerId(200L)
                .active(true)
                .build();
        setAuditFields(business2);
        business2.getCategoryIds().add(2L);

        businessRepository.save(business1);
        businessRepository.save(business2);

        // When
        var ownerBusinesses = businessRepository.findAll().stream()
                .filter(b -> b.getOwnerId().equals(200L))
                .toList();

        // Then
        assertThat(ownerBusinesses).hasSize(2);
        assertThat(ownerBusinesses).extracting("ownerId").containsOnly(200L);
    }

    @Test
    void shouldHandleMultipleCategoriesForBusiness() {
        // Given
        Point location = geometryFactory.createPoint(new Coordinate(14.4378, 50.0755));
        Address address = new Address();
        address.setFormattedAddress("Multi Street 10");
        
        Business business = Business.builder()
                .name("Multi-Category Business")
                .description("Offers multiple services")
                .phoneNumber("+420333333333")
                .email("multi@example.cz")
                .address(address)
                .location(location)
                .ownerId(104L)
                .active(true)
                .build();
        setAuditFields(business);
        business.getCategoryIds().addAll(Set.of(1L, 2L, 3L, 4L));

        // When
        Business savedBusiness = businessRepository.save(business);

        // Then
        assertThat(savedBusiness.getCategoryIds()).hasSize(4);
        assertThat(savedBusiness.getCategoryIds()).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);

        // Verify we can retrieve it
        Business found = businessRepository.findById(savedBusiness.getId()).orElseThrow();
        assertThat(found.getCategoryIds()).hasSize(4);
    }
}
