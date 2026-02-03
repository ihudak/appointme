package eu.dec21.appointme.businesses.businesses.repository;


import eu.dec21.appointme.businesses.businesses.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE c = :categoryId")
    Page<Business> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Business b JOIN b.categoryIds c WHERE c IN :categoryIds ORDER BY b.weightedRating DESC")
    Page<Business> findByCategoryIdIn(@Param("categoryIds") Set<Long> categoryIds, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Business b LEFT JOIN b.keywords k " +
           "WHERE (LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "OR (LOWER(k.keyword) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND k.locale = :locale)")
    Page<Business> searchByKeywordsAndName(
            @Param("searchTerm") String searchTerm,
            @Param("locale") String locale,
            Pageable pageable
    );

    @Query("SELECT b FROM Business b WHERE b.name LIKE %:name%")
    Page<Business> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE c = :categoryId AND b.name LIKE %:name%")
    Page<Business> findByCategoryIdAndNameContaining(@Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.ownerId = :ownerId")
    Page<Business> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.ownerId = :ownerId AND b.name LIKE %:name%")
    Page<Business> findByOwnerIdAndNameContaining(@Param("ownerId") Long ownerId, @Param("name") String name, Pageable pageable);

    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE b.ownerId = :ownerId AND c = :categoryId")
    Page<Business> findByOwnerIdAndCategoryId(@Param("ownerId") Long ownerId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Business b JOIN b.categoryIds c WHERE b.ownerId = :ownerId AND c = :categoryId AND b.name LIKE %:name%")
    Page<Business> findByOwnerIdAndCategoryIdAndNameContaining(@Param("ownerId") Long ownerId, @Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.id = :businessId AND b.ownerId = :ownerId")
    Business findByIdAndOwnerId(@Param("businessId") Long businessId, @Param("ownerId") Long ownerId);
}
