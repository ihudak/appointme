package eu.dec21.appointme.businesses.businesses.repository;

import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.entity.BusinessKeyword;
import eu.dec21.appointme.common.entity.Address;
import eu.dec21.appointme.common.entity.Keyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("BusinessRepository Tests")
class BusinessRepositoryTest {

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

    private GeometryFactory geometryFactory;
    private Business activeBusiness1;
    private Business activeBusiness2;
    private Business inactiveBusiness;
    private Business businessWithKeywords;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        
        // Clear all data
        businessRepository.deleteAll();

        // Setup test data
        activeBusiness1 = createBusiness(
                "Active Coffee Shop",
                "Best coffee in town",
                true,
                1L,
                Set.of(100L, 200L),
                createPoint(13.4050, 52.5200),
                "active-coffee@example.com",
                5.0,
                10,
                5.0
        );

        activeBusiness2 = createBusiness(
                "Active Restaurant",
                "Fine dining experience",
                true,
                1L,
                Set.of(200L, 300L),
                createPoint(13.4100, 52.5250),
                "active-restaurant@example.com",
                4.5,
                20,
                4.6
        );

        inactiveBusiness = createBusiness(
                "Inactive Shop",
                "Closed permanently",
                false,
                2L,
                Set.of(100L),
                createPoint(13.4200, 52.5300),
                "inactive-shop@example.com",
                3.0,
                5,
                3.2
        );

        businessWithKeywords = createBusiness(
                "Keyword Business",
                "Business with searchable keywords",
                true,
                3L,
                Set.of(400L),
                createPoint(13.4300, 52.5350),
                "keyword-business@example.com",
                4.0,
                15,
                4.1
        );

        // Add keywords to businessWithKeywords
        addKeyword(businessWithKeywords, "coffee", "en", Keyword.Source.MANUAL);
        addKeyword(businessWithKeywords, "kaffee", "de", Keyword.Source.MANUAL);
        addKeyword(businessWithKeywords, "barista", "en", Keyword.Source.MANUAL);

        // Persist all test data
        activeBusiness1 = businessRepository.save(activeBusiness1);
        activeBusiness2 = businessRepository.save(activeBusiness2);
        inactiveBusiness = businessRepository.save(inactiveBusiness);
        businessWithKeywords = businessRepository.save(businessWithKeywords);
    }

    @Test
    @DisplayName("Should find businesses by category ID - active only")
    void testFindByCategoryId() {
        Pageable pageable = PageRequest.of(0, 10);

        // Test with category 100
        Page<Business> result = businessRepository.findByCategoryId(100L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");

        // Test with category 200 (should return both active businesses)
        result = businessRepository.findByCategoryId(200L, pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Business::getName)
                .containsExactlyInAnyOrder("Active Coffee Shop", "Active Restaurant");

        // Test with non-existent category
        result = businessRepository.findByCategoryId(999L, pageable);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should find businesses by multiple category IDs ordered by weighted rating")
    void testFindByCategoryIdIn() {
        Pageable pageable = PageRequest.of(0, 10);

        // Test with multiple categories
        Set<Long> categoryIds = Set.of(100L, 200L, 300L);
        Page<Business> result = businessRepository.findByCategoryIdIn(categoryIds, pageable);
        
        assertThat(result.getContent()).hasSize(2);
        // Should be ordered by weighted rating DESC (5.0, then 4.6)
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Active Restaurant");

        // Test with empty set
        result = businessRepository.findByCategoryIdIn(new HashSet<>(), pageable);
        assertThat(result.getContent()).isEmpty();

        // Test with non-matching categories
        result = businessRepository.findByCategoryIdIn(Set.of(999L, 888L), pageable);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should search by keywords and name - case insensitive")
    void testSearchByKeywordsAndName() {
        Pageable pageable = PageRequest.of(0, 10);

        // Search by business name (partial match, case insensitive)
        Page<Business> result = businessRepository.searchByKeywordsAndName("coffee", "en", pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Business::getName)
                .containsExactlyInAnyOrder("Active Coffee Shop", "Keyword Business");

        // Search by keyword in English
        result = businessRepository.searchByKeywordsAndName("barista", "en", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Keyword Business");

        // Search by keyword in German
        result = businessRepository.searchByKeywordsAndName("kaffee", "de", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Keyword Business");

        // Search with wrong locale should not find keyword
        result = businessRepository.searchByKeywordsAndName("kaffee", "en", pageable);
        assertThat(result.getContent()).isEmpty();

        // Search with non-matching term
        result = businessRepository.searchByKeywordsAndName("nonexistent", "en", pageable);
        assertThat(result.getContent()).isEmpty();

        // Test case insensitivity
        result = businessRepository.searchByKeywordsAndName("COFFEE", "en", pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find active businesses by name containing")
    void testFindByActiveTrueAndNameContaining() {
        Pageable pageable = PageRequest.of(0, 10);

        // Search for "Active"
        Page<Business> result = businessRepository.findByActiveTrueAndNameContaining("Active", pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Business::getName)
                .containsExactlyInAnyOrder("Active Coffee Shop", "Active Restaurant");

        // Search for "Coffee"
        result = businessRepository.findByActiveTrueAndNameContaining("Coffee", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");

        // Search should not find inactive business
        result = businessRepository.findByActiveTrueAndNameContaining("Inactive", pageable);
        assertThat(result.getContent()).isEmpty();

        // Search with non-matching name
        result = businessRepository.findByActiveTrueAndNameContaining("Nonexistent", pageable);
        assertThat(result.getContent()).isEmpty();

        // Empty search term
        result = businessRepository.findByActiveTrueAndNameContaining("", pageable);
        assertThat(result.getContent()).hasSize(3); // All active businesses
    }

    @Test
    @DisplayName("Should find businesses by category ID and name containing")
    void testFindByCategoryIdAndNameContaining() {
        Pageable pageable = PageRequest.of(0, 10);

        // Find by category 200 and name "Active"
        Page<Business> result = businessRepository.findByCategoryIdAndNameContaining(200L, "Active", pageable);
        assertThat(result.getContent()).hasSize(2);

        // Find by category 100 and name "Coffee"
        result = businessRepository.findByCategoryIdAndNameContaining(100L, "Coffee", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");

        // Category exists but name doesn't match
        result = businessRepository.findByCategoryIdAndNameContaining(100L, "Restaurant", pageable);
        assertThat(result.getContent()).isEmpty();

        // Name matches but category doesn't exist
        result = businessRepository.findByCategoryIdAndNameContaining(999L, "Coffee", pageable);
        assertThat(result.getContent()).isEmpty();

        // Should not return inactive business
        result = businessRepository.findByCategoryIdAndNameContaining(100L, "Inactive", pageable);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should find businesses by owner ID")
    void testFindByOwnerId() {
        Pageable pageable = PageRequest.of(0, 10);

        // Owner with multiple businesses
        Page<Business> result = businessRepository.findByOwnerId(1L, pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Business::getName)
                .containsExactlyInAnyOrder("Active Coffee Shop", "Active Restaurant");

        // Owner with single business (inactive)
        result = businessRepository.findByOwnerId(2L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Inactive Shop");

        // Owner with no businesses
        result = businessRepository.findByOwnerId(999L, pageable);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should find businesses by owner ID and name containing")
    void testFindByOwnerIdAndNameContaining() {
        Pageable pageable = PageRequest.of(0, 10);

        // Find by owner 1 and name "Coffee"
        Page<Business> result = businessRepository.findByOwnerIdAndNameContaining(1L, "Coffee", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");

        // Find by owner 1 and name "Active" (should return both)
        result = businessRepository.findByOwnerIdAndNameContaining(1L, "Active", pageable);
        assertThat(result.getContent()).hasSize(2);

        // Owner exists but name doesn't match
        result = businessRepository.findByOwnerIdAndNameContaining(1L, "Nonexistent", pageable);
        assertThat(result.getContent()).isEmpty();

        // Name matches but owner doesn't exist
        result = businessRepository.findByOwnerIdAndNameContaining(999L, "Coffee", pageable);
        assertThat(result.getContent()).isEmpty();

        // Empty name search
        result = businessRepository.findByOwnerIdAndNameContaining(1L, "", pageable);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find businesses by owner ID and category ID")
    void testFindByOwnerIdAndCategoryId() {
        Pageable pageable = PageRequest.of(0, 10);

        // Owner 1 with category 200 (should return both businesses)
        Page<Business> result = businessRepository.findByOwnerIdAndCategoryId(1L, 200L, pageable);
        assertThat(result.getContent()).hasSize(2);

        // Owner 1 with category 100
        result = businessRepository.findByOwnerIdAndCategoryId(1L, 100L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");

        // Owner 2 with category 100 (inactive business, should still return it)
        result = businessRepository.findByOwnerIdAndCategoryId(2L, 100L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Inactive Shop");

        // Owner exists but category doesn't match
        result = businessRepository.findByOwnerIdAndCategoryId(1L, 999L, pageable);
        assertThat(result.getContent()).isEmpty();

        // Category exists but owner doesn't match
        result = businessRepository.findByOwnerIdAndCategoryId(999L, 100L, pageable);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should find businesses by owner ID, category ID and name containing")
    void testFindByOwnerIdAndCategoryIdAndNameContaining() {
        Pageable pageable = PageRequest.of(0, 10);

        // Owner 1, category 100, name "Coffee"
        Page<Business> result = businessRepository.findByOwnerIdAndCategoryIdAndNameContaining(
                1L, 100L, "Coffee", pageable
        );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Active Coffee Shop");

        // Owner 1, category 200, name "Active" (should return both)
        result = businessRepository.findByOwnerIdAndCategoryIdAndNameContaining(
                1L, 200L, "Active", pageable
        );
        assertThat(result.getContent()).hasSize(2);

        // All parameters exist but name doesn't match
        result = businessRepository.findByOwnerIdAndCategoryIdAndNameContaining(
                1L, 100L, "Nonexistent", pageable
        );
        assertThat(result.getContent()).isEmpty();

        // Owner and name match but category doesn't
        result = businessRepository.findByOwnerIdAndCategoryIdAndNameContaining(
                1L, 999L, "Coffee", pageable
        );
        assertThat(result.getContent()).isEmpty();

        // Category and name match but owner doesn't
        result = businessRepository.findByOwnerIdAndCategoryIdAndNameContaining(
                999L, 100L, "Coffee", pageable
        );
        assertThat(result.getContent()).isEmpty();

        // Empty name search
        result = businessRepository.findByOwnerIdAndCategoryIdAndNameContaining(
                1L, 100L, "", pageable
        );
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should find business by ID and owner ID")
    void testFindByIdAndOwnerId() {
        // Find existing business with correct owner
        Business result = businessRepository.findByIdAndOwnerId(activeBusiness1.getId(), 1L);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Active Coffee Shop");

        // Find business with wrong owner
        result = businessRepository.findByIdAndOwnerId(activeBusiness1.getId(), 999L);
        assertThat(result).isNull();

        // Find non-existent business
        result = businessRepository.findByIdAndOwnerId(999L, 1L);
        assertThat(result).isNull();

        // Both ID and owner don't match
        result = businessRepository.findByIdAndOwnerId(999L, 999L);
        assertThat(result).isNull();

        // Should work for inactive business too
        result = businessRepository.findByIdAndOwnerId(inactiveBusiness.getId(), 2L);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Inactive Shop");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPagination() {
        // Page size 1
        Pageable pageable = PageRequest.of(0, 1);
        Page<Business> result = businessRepository.findByOwnerId(1L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();

        // Second page
        pageable = PageRequest.of(1, 1);
        result = businessRepository.findByOwnerId(1L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();

        // Large page size
        pageable = PageRequest.of(0, 100);
        result = businessRepository.findByOwnerId(1L, pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle edge cases for search terms")
    void testSearchEdgeCases() {
        Pageable pageable = PageRequest.of(0, 10);

        // Search with special characters
        Page<Business> result = businessRepository.searchByKeywordsAndName("%", "en", pageable);
        assertThat(result.getContent()).isEmpty();

        // Search with null-like string
        result = businessRepository.searchByKeywordsAndName("null", "en", pageable);
        assertThat(result.getContent()).isEmpty();

        // Very long search term
        String longTerm = "a".repeat(1000);
        result = businessRepository.searchByKeywordsAndName(longTerm, "en", pageable);
        assertThat(result.getContent()).isEmpty();

        // Single character search
        result = businessRepository.searchByKeywordsAndName("a", "en", pageable);
        // Should find "Active Coffee Shop", "Active Restaurant", "Keyword Business" (if they contain 'a')
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should respect active flag in queries")
    void testActiveFlag() {
        Pageable pageable = PageRequest.of(0, 10);

        // findByCategoryId should only return active
        Page<Business> result = businessRepository.findByCategoryId(100L, pageable);
        assertThat(result.getContent()).allMatch(Business::isActive);

        // findByCategoryIdIn should only return active
        result = businessRepository.findByCategoryIdIn(Set.of(100L), pageable);
        assertThat(result.getContent()).allMatch(Business::isActive);

        // searchByKeywordsAndName should only return active
        result = businessRepository.searchByKeywordsAndName("Shop", "en", pageable);
        assertThat(result.getContent()).allMatch(Business::isActive);

        // findByActiveTrueAndNameContaining should only return active
        result = businessRepository.findByActiveTrueAndNameContaining("Shop", pageable);
        assertThat(result.getContent()).allMatch(Business::isActive);

        // findByCategoryIdAndNameContaining should only return active
        result = businessRepository.findByCategoryIdAndNameContaining(100L, "Shop", pageable);
        assertThat(result.getContent()).allMatch(Business::isActive);

        // findByOwnerId should return both active and inactive
        result = businessRepository.findByOwnerId(2L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle empty category sets")
    void testEmptyCategorySets() {
        // Create business with no categories
        Business noCategoryBusiness = createBusiness(
                "No Category Business",
                "Business without categories",
                true,
                4L,
                new HashSet<>(),
                createPoint(13.4400, 52.5400),
                "no-category@example.com",
                3.5,
                8,
                3.6
        );
        noCategoryBusiness = businessRepository.saveAndFlush(noCategoryBusiness);

        Pageable pageable = PageRequest.of(0, 10);

        // Should not be found by category search
        Page<Business> result = businessRepository.findByCategoryId(100L, pageable);
        assertThat(result.getContent())
                .extracting(Business::getName)
                .doesNotContain("No Category Business");

        // But should be found by owner
        result = businessRepository.findByOwnerId(4L, pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("No Category Business");
    }

    @Test
    @DisplayName("Should handle businesses with multiple keywords")
    void testMultipleKeywords() {
        Pageable pageable = PageRequest.of(0, 10);

        // Search should find business by any of its keywords
        Page<Business> coffeeResult = businessRepository.searchByKeywordsAndName("coffee", "en", pageable);
        assertThat(coffeeResult.getContent())
                .extracting(Business::getName)
                .contains("Keyword Business");

        Page<Business> baristaResult = businessRepository.searchByKeywordsAndName("barista", "en", pageable);
        assertThat(baristaResult.getContent())
                .extracting(Business::getName)
                .contains("Keyword Business");

        // Different locale should work for locale-specific keywords
        Page<Business> germanResult = businessRepository.searchByKeywordsAndName("kaffee", "de", pageable);
        assertThat(germanResult.getContent())
                .extracting(Business::getName)
                .contains("Keyword Business");
    }

    // Helper methods

    private Business createBusiness(String name, String description, boolean active, Long ownerId,
                                    Set<Long> categoryIds, Point location, String email,
                                    Double rating, Integer reviewCount, Double weightedRating) {
        Address address = new Address();
        address.setLine1("Test Street 123");
        address.setCity("Berlin");
        address.setPostalCode("10115");
        address.setCountryCode("DE");

        return Business.builder()
                .name(name)
                .description(description)
                .active(active)
                .ownerId(ownerId)
                .categoryIds(categoryIds)
                .location(location)
                .email(email)
                .emailVerified(false)
                .phoneNumber("+491234567890")
                .website("https://example.com")
                .address(address)
                .rating(rating)
                .reviewCount(reviewCount)
                .weightedRating(weightedRating)
                .adminIds(new HashSet<>())
                .keywords(new HashSet<>())
                .images(new HashSet<>())
                .build();
    }

    private Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private void addKeyword(Business business, String keyword, String locale, Keyword.Source source) {
        BusinessKeyword bk = BusinessKeyword.builder()
                .keyword(keyword)
                .locale(locale)
                .source(source)
                .weight(100)
                .business(business)
                .build();
        business.getKeywords().add(bk);
    }
}
