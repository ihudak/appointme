package eu.dec21.appointme.businesses.businesses.service;

import eu.dec21.appointme.businesses.businesses.config.RatingConfig;
import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.mapper.BusinessMapper;
import eu.dec21.appointme.businesses.businesses.repository.BusinessRepository;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.client.CategoryFeignClient;
import eu.dec21.appointme.common.entity.Address;
import eu.dec21.appointme.common.response.PageResponse;
import eu.dec21.appointme.exceptions.OperationNotPermittedException;
import eu.dec21.appointme.exceptions.UserAuthenticationException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BusinessService.
 * Tests all public methods including public, owner, and admin operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessService Tests")
class BusinessServiceTest {

    @Mock
    private BusinessMapper businessMapper;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private RatingConfig ratingConfig;

    @Mock
    private CategoryFeignClient categoryFeignClient;

    @InjectMocks
    private BusinessService businessService;

    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    // ==================== Public Methods Tests ====================

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return BusinessResponse when business exists and is active")
        void testFindById_Success() {
            // Given
            Long businessId = 1L;
            Business business = createBusiness(businessId, "Test Business", true);
            BusinessResponse response = createBusinessResponse(businessId, "Test Business");

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            BusinessResponse result = businessService.findById(businessId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(businessId);
            assertThat(result.getName()).isEqualTo("Test Business");

            verify(businessRepository).findById(businessId);
            verify(businessMapper).toBusinessResponse(business);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void testFindById_NotFound() {
            // Given
            Long businessId = 999L;
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> businessService.findById(businessId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId);

            verify(businessRepository).findById(businessId);
            verify(businessMapper, never()).toBusinessResponse(any());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business exists but is inactive")
        void testFindById_Inactive() {
            // Given
            Long businessId = 1L;
            Business business = createBusiness(businessId, "Inactive Business", false);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

            // When/Then
            assertThatThrownBy(() -> businessService.findById(businessId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId);

            verify(businessRepository).findById(businessId);
            verify(businessMapper, never()).toBusinessResponse(any());
        }
    }

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return paginated list of active businesses sorted by weighted rating")
        void testFindAll_Success() {
            // Given
            Business business1 = createBusiness(1L, "Business 1", true);
            Business business2 = createBusiness(2L, "Business 2", true);
            
            List<Business> businesses = Arrays.asList(business1, business2);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse response1 = createBusinessResponse(1L, "Business 1");
            BusinessResponse response2 = createBusinessResponse(2L, "Business 2");

            when(businessRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(business1)).thenReturn(response1);
            when(businessMapper.toBusinessResponse(business2)).thenReturn(response2);

            // When
            PageResponse<BusinessResponse> result = businessService.findAll(0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getPageNumber()).isEqualTo(0);
            assertThat(result.getPageSize()).isEqualTo(10);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(businessRepository).findAll(pageableCaptor.capture());
            
            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            assertThat(capturedPageable.getSort().getOrderFor("weightedRating")).isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("weightedRating").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("Should filter out inactive businesses from results")
        void testFindAll_FiltersInactive() {
            // Given
            Business activeBusiness = createBusiness(1L, "Active", true);
            Business inactiveBusiness = createBusiness(2L, "Inactive", false);
            
            List<Business> businesses = Arrays.asList(activeBusiness, inactiveBusiness);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse activeResponse = createBusinessResponse(1L, "Active");

            when(businessRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(activeBusiness)).thenReturn(activeResponse);

            // When
            PageResponse<BusinessResponse> result = businessService.findAll(0, 10);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Active");

            verify(businessMapper).toBusinessResponse(activeBusiness);
            verify(businessMapper, never()).toBusinessResponse(inactiveBusiness);
        }

        @Test
        @DisplayName("Should return empty page when no businesses exist")
        void testFindAll_Empty() {
            // Given
            Page<Business> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(businessRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            PageResponse<BusinessResponse> result = businessService.findAll(0, 10);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByCategory() Tests")
    class FindByCategoryTests {

        @Test
        @DisplayName("Should return businesses for specific category")
        void testFindByCategory_Success() {
            // Given
            Long categoryId = 5L;
            Business business1 = createBusiness(1L, "Business 1", true);
            Business business2 = createBusiness(2L, "Business 2", true);
            
            List<Business> businesses = Arrays.asList(business1, business2);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse response1 = createBusinessResponse(1L, "Business 1");
            BusinessResponse response2 = createBusinessResponse(2L, "Business 2");

            when(businessRepository.findByCategoryId(eq(categoryId), any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(business1)).thenReturn(response1);
            when(businessMapper.toBusinessResponse(business2)).thenReturn(response2);

            // When
            PageResponse<BusinessResponse> result = businessService.findByCategory(categoryId, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);

            verify(businessRepository).findByCategoryId(eq(categoryId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no businesses in category")
        void testFindByCategory_Empty() {
            // Given
            Long categoryId = 5L;
            Page<Business> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(businessRepository.findByCategoryId(eq(categoryId), any(Pageable.class))).thenReturn(emptyPage);

            // When
            PageResponse<BusinessResponse> result = businessService.findByCategory(categoryId, 0, 10);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByCategoryWithSubcategories() Tests")
    class FindByCategoryWithSubcategoriesTests {

        @Test
        @DisplayName("Should fetch subcategories and return businesses from all categories")
        void testFindByCategoryWithSubcategories_Success() {
            // Given
            Long categoryId = 5L;
            Set<Long> subcategoryIds = Set.of(6L, 7L, 8L);
            
            Business business1 = createBusiness(1L, "Business 1", true);
            Business business2 = createBusiness(2L, "Business 2", true);
            
            List<Business> businesses = Arrays.asList(business1, business2);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse response1 = createBusinessResponse(1L, "Business 1");
            BusinessResponse response2 = createBusinessResponse(2L, "Business 2");

            when(categoryFeignClient.getAllSubcategoryIds(categoryId)).thenReturn(subcategoryIds);
            when(businessRepository.findByCategoryIdIn(any(Set.class), any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(business1)).thenReturn(response1);
            when(businessMapper.toBusinessResponse(business2)).thenReturn(response2);

            // When
            PageResponse<BusinessResponse> result = businessService.findByCategoryWithSubcategories(categoryId, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            ArgumentCaptor<Set<Long>> categoryIdsCaptor = ArgumentCaptor.forClass(Set.class);
            verify(categoryFeignClient).getAllSubcategoryIds(categoryId);
            verify(businessRepository).findByCategoryIdIn(categoryIdsCaptor.capture(), any(Pageable.class));
            
            Set<Long> capturedCategoryIds = categoryIdsCaptor.getValue();
            assertThat(capturedCategoryIds).contains(5L, 6L, 7L, 8L);
        }

        @Test
        @DisplayName("Should include parent category even if no subcategories exist")
        void testFindByCategoryWithSubcategories_NoSubcategories() {
            // Given
            Long categoryId = 5L;
            Set<Long> subcategoryIds = Collections.emptySet();
            
            Page<Business> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

            when(categoryFeignClient.getAllSubcategoryIds(categoryId)).thenReturn(subcategoryIds);
            when(businessRepository.findByCategoryIdIn(any(Set.class), any(Pageable.class))).thenReturn(emptyPage);

            // When
            PageResponse<BusinessResponse> result = businessService.findByCategoryWithSubcategories(categoryId, 0, 10);

            // Then
            ArgumentCaptor<Set<Long>> categoryIdsCaptor = ArgumentCaptor.forClass(Set.class);
            verify(businessRepository).findByCategoryIdIn(categoryIdsCaptor.capture(), any(Pageable.class));
            
            Set<Long> capturedCategoryIds = categoryIdsCaptor.getValue();
            assertThat(capturedCategoryIds).containsExactly(5L);
        }
    }

    // ==================== Owner Methods Tests ====================

    @Nested
    @DisplayName("createBusiness() Tests")
    class CreateBusinessTests {

        @Test
        @DisplayName("Should create business with authenticated owner")
        void testCreateBusiness_Success() {
            // Given
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            BusinessRequest request = new BusinessRequest(
                    null,
                    "New Business",
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null
            );
            
            Business business = createBusiness(null, "New Business", true);
            Business savedBusiness = createBusiness(1L, "New Business", true);
            BusinessResponse response = createBusinessResponse(1L, "New Business");

            when(businessMapper.toBusiness(request)).thenReturn(business);
            when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);
            when(businessMapper.toBusinessResponse(savedBusiness)).thenReturn(response);

            // When
            BusinessResponse result = businessService.createBusiness(request, auth);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("New Business");

            ArgumentCaptor<Business> businessCaptor = ArgumentCaptor.forClass(Business.class);
            verify(businessRepository).save(businessCaptor.capture());
            
            Business capturedBusiness = businessCaptor.getValue();
            assertThat(capturedBusiness.getOwnerId()).isEqualTo(ownerId);
        }

        @Test
        @DisplayName("Should throw UserAuthenticationException when user not authenticated")
        void testCreateBusiness_NotAuthenticated() {
            // Given
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then
            assertThatThrownBy(() -> businessService.createBusiness(request, auth))
                    .isInstanceOf(UserAuthenticationException.class);

            verify(businessRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByOwner() Tests")
    class FindByOwnerTests {

        @Test
        @DisplayName("Should return businesses owned by authenticated user")
        void testFindByOwner_Success() {
            // Given
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business1 = createBusiness(1L, "Business 1", true);
            Business business2 = createBusiness(2L, "Business 2", false);
            business1.setOwnerId(ownerId);
            business2.setOwnerId(ownerId);
            
            List<Business> businesses = Arrays.asList(business1, business2);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse response1 = createBusinessResponse(1L, "Business 1");
            BusinessResponse response2 = createBusinessResponse(2L, "Business 2");

            when(businessRepository.findByOwnerId(eq(ownerId), any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(business1)).thenReturn(response1);
            when(businessMapper.toBusinessResponse(business2)).thenReturn(response2);

            // When
            PageResponse<BusinessResponse> result = businessService.findByOwner(auth, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            verify(businessRepository).findByOwnerId(eq(ownerId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("findByIdAndOwner() Tests")
    class FindByIdAndOwnerTests {

        @Test
        @DisplayName("Should return business when owned by authenticated user")
        void testFindByIdAndOwner_Success() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business = createBusiness(businessId, "My Business", true);
            business.setOwnerId(ownerId);
            BusinessResponse response = createBusinessResponse(businessId, "My Business");

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            BusinessResponse result = businessService.findByIdAndOwner(businessId, auth);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(businessId);

            verify(businessRepository).findByIdAndOwnerId(businessId, ownerId);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business not owned by user")
        void testFindByIdAndOwner_NotOwner() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> businessService.findByIdAndOwner(businessId, auth))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId + " for current owner");

            verify(businessRepository).findByIdAndOwnerId(businessId, ownerId);
        }
    }

    @Nested
    @DisplayName("updateBusinessByOwner() Tests")
    class UpdateBusinessByOwnerTests {

        @Test
        @DisplayName("Should update business fields when owned by user")
        void testUpdateBusinessByOwner_Success() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business = createBusiness(businessId, "Old Name", true);
            business.setOwnerId(ownerId);
            
            Address newAddress = new Address();
            newAddress.setCity("Berlin");
            
            Point newLocation = createPoint(13.4050, 52.5200);
            
            BusinessRequest request = new BusinessRequest(
                    null,
                    "New Name",
                    "New Description",
                    newAddress,
                    newLocation,
                    "+491234567890",
                    "https://new-website.com",
                    "new@email.com"
            );
            
            BusinessResponse response = createBusinessResponse(businessId, "New Name");

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(business);
            when(businessRepository.save(business)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            BusinessResponse result = businessService.updateBusinessByOwner(businessId, request, auth);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Name");

            assertThat(business.getName()).isEqualTo("New Name");
            assertThat(business.getDescription()).isEqualTo("New Description");
            assertThat(business.getAddress()).isEqualTo(newAddress);
            assertThat(business.getLocation()).isEqualTo(newLocation);
            assertThat(business.getPhoneNumber()).isEqualTo("+491234567890");
            assertThat(business.getWebsite()).isEqualTo("https://new-website.com");
            assertThat(business.getEmail()).isEqualTo("new@email.com");

            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("Should only update non-null fields")
        void testUpdateBusinessByOwner_PartialUpdate() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business = createBusiness(businessId, "Old Name", true);
            business.setOwnerId(ownerId);
            business.setDescription("Old Description");
            business.setEmail("old@email.com");
            
            BusinessRequest request = new BusinessRequest(
                    null,
                    "New Name",
                    null, // description not updated
                    null,
                    null,
                    null,
                    null,
                    null  // email not updated
            );
            
            BusinessResponse response = createBusinessResponse(businessId, "New Name");

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(business);
            when(businessRepository.save(business)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            businessService.updateBusinessByOwner(businessId, request, auth);

            // Then - Only name should be updated
            assertThat(business.getName()).isEqualTo("New Name");
            assertThat(business.getDescription()).isEqualTo("Old Description"); // Not changed
            assertThat(business.getEmail()).isEqualTo("old@email.com"); // Not changed
        }

        @Test
        @DisplayName("Should throw OperationNotPermittedException when not owner")
        void testUpdateBusinessByOwner_NotOwner() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            BusinessRequest request = new BusinessRequest(null, "New Name", null, null, null, null, null, null);

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> businessService.updateBusinessByOwner(businessId, request, auth))
                    .isInstanceOf(OperationNotPermittedException.class)
                    .hasMessageContaining("You do not have permission to update this business");

            verify(businessRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("toggleBusinessActiveByOwner() Tests")
    class ToggleBusinessActiveByOwnerTests {

        @Test
        @DisplayName("Should activate business when owned by user")
        void testToggleActive_Activate() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business = createBusiness(businessId, "Business", false);
            business.setOwnerId(ownerId);
            BusinessResponse response = createBusinessResponse(businessId, "Business");

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(business);
            when(businessRepository.save(business)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            businessService.toggleBusinessActiveByOwner(businessId, true, auth);

            // Then
            assertThat(business.isActive()).isTrue();
            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("Should deactivate business when owned by user")
        void testToggleActive_Deactivate() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business = createBusiness(businessId, "Business", true);
            business.setOwnerId(ownerId);
            BusinessResponse response = createBusinessResponse(businessId, "Business");

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(business);
            when(businessRepository.save(business)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            businessService.toggleBusinessActiveByOwner(businessId, false, auth);

            // Then
            assertThat(business.isActive()).isFalse();
            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("Should throw OperationNotPermittedException when not owner")
        void testToggleActive_NotOwner() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> businessService.toggleBusinessActiveByOwner(businessId, true, auth))
                    .isInstanceOf(OperationNotPermittedException.class)
                    .hasMessageContaining("You do not have permission to modify this business");

            verify(businessRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteBusinessByOwner() Tests")
    class DeleteBusinessByOwnerTests {

        @Test
        @DisplayName("Should delete business when owned by user")
        void testDeleteByOwner_Success() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);
            
            Business business = createBusiness(businessId, "Business", true);
            business.setOwnerId(ownerId);

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(business);

            // When
            businessService.deleteBusinessByOwner(businessId, auth);

            // Then
            verify(businessRepository).delete(business);
        }

        @Test
        @DisplayName("Should throw OperationNotPermittedException when not owner")
        void testDeleteByOwner_NotOwner() {
            // Given
            Long businessId = 1L;
            Long ownerId = 100L;
            Authentication auth = createMockAuthentication(ownerId);

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> businessService.deleteBusinessByOwner(businessId, auth))
                    .isInstanceOf(OperationNotPermittedException.class)
                    .hasMessageContaining("You do not have permission to delete this business");

            verify(businessRepository, never()).delete(any());
        }
    }

    // ==================== Admin Methods Tests ====================

    @Nested
    @DisplayName("findAllBusinesses() Tests")
    class FindAllBusinessesTests {

        @Test
        @DisplayName("Should return all businesses including inactive when includeInactive is true")
        void testFindAllBusinesses_IncludeInactive() {
            // Given
            Business activeBusiness = createBusiness(1L, "Active", true);
            Business inactiveBusiness = createBusiness(2L, "Inactive", false);
            
            List<Business> businesses = Arrays.asList(activeBusiness, inactiveBusiness);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse activeResponse = createBusinessResponse(1L, "Active");
            BusinessResponse inactiveResponse = createBusinessResponse(2L, "Inactive");

            when(businessRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(activeBusiness)).thenReturn(activeResponse);
            when(businessMapper.toBusinessResponse(inactiveBusiness)).thenReturn(inactiveResponse);

            // When
            PageResponse<BusinessResponse> result = businessService.findAllBusinesses(0, 10, true);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(BusinessResponse::getName)
                    .containsExactly("Active", "Inactive");

            verify(businessMapper).toBusinessResponse(activeBusiness);
            verify(businessMapper).toBusinessResponse(inactiveBusiness);
        }

        @Test
        @DisplayName("Should filter out inactive businesses when includeInactive is false")
        void testFindAllBusinesses_ExcludeInactive() {
            // Given
            Business activeBusiness = createBusiness(1L, "Active", true);
            Business inactiveBusiness = createBusiness(2L, "Inactive", false);
            
            List<Business> businesses = Arrays.asList(activeBusiness, inactiveBusiness);
            Page<Business> page = new PageImpl<>(businesses, PageRequest.of(0, 10), 2);

            BusinessResponse activeResponse = createBusinessResponse(1L, "Active");

            when(businessRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(businessMapper.toBusinessResponse(activeBusiness)).thenReturn(activeResponse);

            // When
            PageResponse<BusinessResponse> result = businessService.findAllBusinesses(0, 10, false);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Active");

            verify(businessMapper).toBusinessResponse(activeBusiness);
            verify(businessMapper, never()).toBusinessResponse(inactiveBusiness);
        }
    }

    @Nested
    @DisplayName("findByIdAdmin() Tests")
    class FindByIdAdminTests {

        @Test
        @DisplayName("Should return business regardless of active status")
        void testFindByIdAdmin_Success() {
            // Given
            Long businessId = 1L;
            Business business = createBusiness(businessId, "Business", false); // Inactive
            BusinessResponse response = createBusinessResponse(businessId, "Business");

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            BusinessResponse result = businessService.findByIdAdmin(businessId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(businessId);

            verify(businessRepository).findById(businessId);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void testFindByIdAdmin_NotFound() {
            // Given
            Long businessId = 999L;
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> businessService.findByIdAdmin(businessId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId);
        }
    }

    @Nested
    @DisplayName("updateBusinessByAdmin() Tests")
    class UpdateBusinessByAdminTests {

        @Test
        @DisplayName("Should update business fields as admin")
        void testUpdateByAdmin_Success() {
            // Given
            Long businessId = 1L;
            Business business = createBusiness(businessId, "Old Name", true);
            
            BusinessRequest request = new BusinessRequest(
                    null,
                    "New Name",
                    "New Description",
                    null,
                    null,
                    null,
                    null,
                    "newemail@example.com"
            );
            
            BusinessResponse response = createBusinessResponse(businessId, "New Name");

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessRepository.save(business)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            BusinessResponse result = businessService.updateBusinessByAdmin(businessId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(business.getName()).isEqualTo("New Name");
            assertThat(business.getDescription()).isEqualTo("New Description");
            assertThat(business.getEmail()).isEqualTo("newemail@example.com");

            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void testUpdateByAdmin_NotFound() {
            // Given
            Long businessId = 999L;
            BusinessRequest request = new BusinessRequest(null, "Name", null, null, null, null, null, null);

            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> businessService.updateBusinessByAdmin(businessId, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId);

            verify(businessRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("toggleBusinessActiveByAdmin() Tests")
    class ToggleBusinessActiveByAdminTests {

        @Test
        @DisplayName("Should toggle business active status as admin")
        void testToggleActiveByAdmin_Success() {
            // Given
            Long businessId = 1L;
            Business business = createBusiness(businessId, "Business", true);
            BusinessResponse response = createBusinessResponse(businessId, "Business");

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessRepository.save(business)).thenReturn(business);
            when(businessMapper.toBusinessResponse(business)).thenReturn(response);

            // When
            businessService.toggleBusinessActiveByAdmin(businessId, false);

            // Then
            assertThat(business.isActive()).isFalse();
            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void testToggleActiveByAdmin_NotFound() {
            // Given
            Long businessId = 999L;
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> businessService.toggleBusinessActiveByAdmin(businessId, true))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId);

            verify(businessRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteBusinessByAdmin() Tests")
    class DeleteBusinessByAdminTests {

        @Test
        @DisplayName("Should delete business as admin")
        void testDeleteByAdmin_Success() {
            // Given
            Long businessId = 1L;
            Business business = createBusiness(businessId, "Business", true);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

            // When
            businessService.deleteBusinessByAdmin(businessId);

            // Then
            verify(businessRepository).delete(business);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void testDeleteByAdmin_NotFound() {
            // Given
            Long businessId = 999L;
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> businessService.deleteBusinessByAdmin(businessId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Business not found with id " + businessId);

            verify(businessRepository, never()).delete(any());
        }
    }

    // ==================== Utility Methods Tests ====================

    @Nested
    @DisplayName("calculateWeightedRating() Tests")
    class CalculateWeightedRatingTests {

        @Test
        @DisplayName("Should calculate weighted rating using config values")
        void testCalculateWeightedRating_Success() {
            // Given
            Business business = createBusiness(1L, "Business", true);
            business.setRating(4.5);
            business.setReviewCount(20);

            when(ratingConfig.getConfidenceThreshold()).thenReturn(10);
            when(ratingConfig.getGlobalMean()).thenReturn(3.5);

            // When
            Double result = businessService.calculateWeightedRating(business);

            // Then
            // Formula: (C × m + n × r) / (C + n)
            // (10 × 3.5 + 20 × 4.5) / (10 + 20) = (35 + 90) / 30 = 125 / 30 = 4.166...
            assertThat(result).isEqualTo(4.166666666666667);

            verify(ratingConfig).getConfidenceThreshold();
            verify(ratingConfig).getGlobalMean();
        }

        @Test
        @DisplayName("Should return 0.0 when rating is null")
        void testCalculateWeightedRating_NullRating() {
            // Given
            Business business = createBusiness(1L, "Business", true);
            business.setRating(null);
            business.setReviewCount(10);

            // When
            Double result = businessService.calculateWeightedRating(business);

            // Then
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return 0.0 when reviewCount is zero")
        void testCalculateWeightedRating_ZeroReviews() {
            // Given
            Business business = createBusiness(1L, "Business", true);
            business.setRating(4.5);
            business.setReviewCount(0);

            // When
            Double result = businessService.calculateWeightedRating(business);

            // Then
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("calculateWeightedRating - Should handle negative rating")
        void testCalculateWeightedRating_NegativeRating() {
            // Given
            Business business = Business.builder()
                    .name("Negative Rating Business")
                    .email("neg@test.com")
                    .rating(-1.0)
                    .reviewCount(5)
                    .build();

            when(ratingConfig.getConfidenceThreshold()).thenReturn(10);
            when(ratingConfig.getGlobalMean()).thenReturn(3.5);

            // When
            Double result = businessService.calculateWeightedRating(business);

            // Then — Bayesian formula: (10 * 3.5 + 5 * -1.0) / (10 + 5) = 30/15 = 2.0
            assertThat(result).isCloseTo(2.0, within(0.01));
        }

        @Test
        @DisplayName("calculateWeightedRating - Should handle very large review count")
        void testCalculateWeightedRating_LargeReviewCount() {
            // Given
            Business business = Business.builder()
                    .name("Popular Business")
                    .email("pop@test.com")
                    .rating(4.8)
                    .reviewCount(1000000)
                    .build();

            when(ratingConfig.getConfidenceThreshold()).thenReturn(10);
            when(ratingConfig.getGlobalMean()).thenReturn(3.5);

            // When
            Double result = businessService.calculateWeightedRating(business);

            // Then — with very high review count, result converges to the actual rating
            assertThat(result).isCloseTo(4.8, within(0.01));
        }

        @Test
        @DisplayName("calculateWeightedRating - Should handle review count of 1")
        void testCalculateWeightedRating_SingleReview() {
            // Given
            Business business = Business.builder()
                    .name("New Business")
                    .email("new@test.com")
                    .rating(5.0)
                    .reviewCount(1)
                    .build();

            when(ratingConfig.getConfidenceThreshold()).thenReturn(10);
            when(ratingConfig.getGlobalMean()).thenReturn(3.5);

            // When
            Double result = businessService.calculateWeightedRating(business);

            // Then — Bayesian: (10 * 3.5 + 1 * 5.0) / (10 + 1) = 40/11 ≈ 3.636
            assertThat(result).isCloseTo(3.636, within(0.01));
        }
    }

    @Nested
    @DisplayName("updateBusinessRating() Tests")
    class UpdateBusinessRatingTests {

        @Test
        @DisplayName("Should update business rating and save to repository")
        void testUpdateBusinessRating_Success() {
            // Given
            Business business = createBusiness(1L, "Business", true);
            business.setRating(3.0);
            business.setReviewCount(5);
            business.setWeightedRating(3.2);

            Double newRating = 4.5;
            Integer newReviewCount = 25;

            when(ratingConfig.getConfidenceThreshold()).thenReturn(10);
            when(ratingConfig.getGlobalMean()).thenReturn(3.5);
            when(businessRepository.save(business)).thenReturn(business);

            // When
            businessService.updateBusinessRating(business, newRating, newReviewCount);

            // Then
            assertThat(business.getRating()).isEqualTo(4.5);
            assertThat(business.getReviewCount()).isEqualTo(25);
            // Weighted rating should be updated
            // Formula: (10 × 3.5 + 25 × 4.5) / (10 + 25) = (35 + 112.5) / 35 = 4.214...
            assertThat(business.getWeightedRating()).isEqualTo(4.214285714285714);

            verify(businessRepository).save(business);
            verify(ratingConfig).getConfidenceThreshold();
            verify(ratingConfig).getGlobalMean();
        }

        @Test
        @DisplayName("Should handle null new rating")
        void testUpdateBusinessRating_NullRating() {
            // Given
            Business business = createBusiness(1L, "Business", true);
            business.setRating(4.0);
            business.setReviewCount(10);

            when(businessRepository.save(business)).thenReturn(business);

            // When
            businessService.updateBusinessRating(business, null, 15);

            // Then
            assertThat(business.getRating()).isNull();
            assertThat(business.getReviewCount()).isEqualTo(15);
            assertThat(business.getWeightedRating()).isEqualTo(0.0); // Calculated as 0 when rating is null

            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("Should handle zero review count")
        void testUpdateBusinessRating_ZeroReviews() {
            // Given
            Business business = createBusiness(1L, "Business", true);

            when(businessRepository.save(business)).thenReturn(business);

            // When
            businessService.updateBusinessRating(business, 4.5, 0);

            // Then
            assertThat(business.getRating()).isEqualTo(4.5);
            assertThat(business.getReviewCount()).isEqualTo(0);
            assertThat(business.getWeightedRating()).isEqualTo(0.0);

            verify(businessRepository).save(business);
        }

        @Test
        @DisplayName("updateBusinessRating - Should handle null rating and null review count")
        void testUpdateBusinessRating_AllNull() {
            // Given
            Business business = Business.builder()
                    .name("Business")
                    .email("test@test.com")
                    .rating(4.0)
                    .reviewCount(10)
                    .build();

            when(businessRepository.save(business)).thenReturn(business);

            // When
            businessService.updateBusinessRating(business, null, null);

            // Then — null rating/reviewCount returns 0.0 weighted rating
            assertThat(business.getRating()).isNull();
            assertThat(business.getReviewCount()).isNull();
            assertThat(business.getWeightedRating()).isEqualTo(0.0);
            verify(businessRepository).save(business);
        }
    }

    // ==================== Helper Methods ====================

    private Business createBusiness(Long id, String name, boolean active) {
        Business business = Business.builder()
                .id(id)
                .name(name)
                .active(active)
                .images(new HashSet<>())
                .build();
        return business;
    }

    private BusinessResponse createBusinessResponse(Long id, String name) {
        return BusinessResponse.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private Authentication createMockAuthentication(Long userId) {
        Authentication auth = mock(Authentication.class);
        TestUserDetails userDetails = new TestUserDetails(userId);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        return auth;
    }
}
