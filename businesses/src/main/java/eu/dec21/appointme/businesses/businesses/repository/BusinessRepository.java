package eu.dec21.appointme.businesses.businesses.repository;


import eu.dec21.appointme.businesses.businesses.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE c = :categoryId AND b.active = true")
    Page<Business> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Business b JOIN b.categoryIds c WHERE c IN :categoryIds AND b.active = true ORDER BY b.weightedRating DESC")
    Page<Business> findByCategoryIdIn(@Param("categoryIds") Set<Long> categoryIds, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Business b LEFT JOIN b.keywords k " +
           "WHERE b.active = true AND ((LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ESCAPE '\\') " +
           "OR (LOWER(k.keyword) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ESCAPE '\\' AND k.locale = :locale))")
    Page<Business> searchByKeywordsAndName(
            @Param("searchTerm") String searchTerm,
            @Param("locale") String locale,
            Pageable pageable
    );

    /**
     * Escapes LIKE wildcard characters (%, _) in user-provided search terms.
     * Must be called before passing search terms to queries using LIKE with ESCAPE '\'.
     */
    default String escapeLikeWildcards(String input) {
        if (input == null) return null;
        return input.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
    }

    Page<Business> findByActiveTrueAndNameContaining(String name, Pageable pageable);

    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE c = :categoryId AND b.active = true AND b.name LIKE CONCAT('%', :name, '%') ESCAPE '\\'")
    Page<Business> findByCategoryIdAndNameContaining(@Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);

    Page<Business> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Business> findByOwnerIdAndNameContaining(Long ownerId, String name, Pageable pageable);

    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE b.ownerId = :ownerId AND c = :categoryId")
    Page<Business> findByOwnerIdAndCategoryId(@Param("ownerId") Long ownerId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE b.ownerId = :ownerId AND c = :categoryId AND b.name LIKE CONCAT('%', :name, '%') ESCAPE '\\'")
    Page<Business> findByOwnerIdAndCategoryIdAndNameContaining(@Param("ownerId") Long ownerId, @Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);

    Business findByIdAndOwnerId(Long businessId, Long ownerId);
}
