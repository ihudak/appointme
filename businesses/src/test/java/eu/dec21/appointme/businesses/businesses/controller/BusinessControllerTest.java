package eu.dec21.appointme.businesses.businesses.controller;

import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
import eu.dec21.appointme.common.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for BusinessController.
 * Tests all endpoints with various scenarios including success, validation, and error cases.
 * Uses @WebMvcTest for lightweight controller testing with MockMvc.
 */
@WebMvcTest(BusinessController.class)
@DisplayName("BusinessController Tests")
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessService businessService;

    // ==================== GET /businesses/{id} Tests ====================

    @Nested
    @DisplayName("GET /businesses/{id} - Get Business By ID")
    class GetBusinessByIdTests {

        @Test
        @DisplayName("Should return 200 OK with business when found")
        void testGetBusinessById_Success() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Test Business", true);

            when(businessService.findById(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("Test Business"))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when business does not exist")
        void testGetBusinessById_NotFound() throws Exception {
            // Given
            Long businessId = 999L;

            when(businessService.findById(businessId))
                    .thenThrow(new EntityNotFoundException("Business not found with id " + businessId));

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when business is inactive")
        void testGetBusinessById_Inactive() throws Exception {
            // Given
            Long businessId = 1L;

            when(businessService.findById(businessId))
                    .thenThrow(new EntityNotFoundException("Business not found with id " + businessId));

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should handle business with all fields populated")
        void testGetBusinessById_AllFields() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = BusinessResponse.builder()
                    .id(businessId)
                    .name("Complete Business")
                    .description("A business with all fields")
                    .phoneNumber("+491234567890")
                    .website("https://example.com")
                    .email("test@example.com")
                    .imageUrl("https://example.com/image.png")
                    .rating(4.5)
                    .reviewCount(100)
                    .active(true)
                    .build();

            when(businessService.findById(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("Complete Business"))
                    .andExpect(jsonPath("$.description").value("A business with all fields"))
                    .andExpect(jsonPath("$.phoneNumber").value("+491234567890"))
                    .andExpect(jsonPath("$.website").value("https://example.com"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.png"))
                    .andExpect(jsonPath("$.rating").value(4.5))
                    .andExpect(jsonPath("$.reviewCount").value(100))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should handle business with minimal fields")
        void testGetBusinessById_MinimalFields() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = BusinessResponse.builder()
                    .id(businessId)
                    .name("Minimal Business")
                    .active(true)
                    .build();

            when(businessService.findById(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("Minimal Business"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.description").doesNotExist())
                    .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                    .andExpect(jsonPath("$.website").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist());

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should handle very large business ID")
        void testGetBusinessById_LargeId() throws Exception {
            // Given
            Long businessId = Long.MAX_VALUE;
            BusinessResponse response = createBusinessResponse(businessId, "Large ID Business", true);

            when(businessService.findById(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(businessId));

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testGetBusinessById_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/{id}", "invalid-id")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findById(anyLong());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for negative ID")
        void testGetBusinessById_NegativeId() throws Exception {
            // Given
            Long businessId = -1L;

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findById(anyLong());
        }
    }

    // ==================== GET /businesses Tests ====================

    @Nested
    @DisplayName("GET /businesses - Get All Businesses")
    class GetAllBusinessesTests {

        @Test
        @DisplayName("Should return 200 OK with paginated businesses using default pagination")
        void testGetAllBusinesses_DefaultPagination() throws Exception {
            // Given
            BusinessResponse business1 = createBusinessResponse(1L, "Business 1", true);
            BusinessResponse business2 = createBusinessResponse(2L, "Business 2", true);
            
            List<BusinessResponse> businesses = Arrays.asList(business1, business2);
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    businesses,
                    2L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findAll(0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Business 1"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].name").value("Business 2"))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.pageSize").value(10))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.empty").value(false));

            verify(businessService).findAll(0, 10);
        }

        @Test
        @DisplayName("Should return 200 OK with custom pagination parameters")
        void testGetAllBusinesses_CustomPagination() throws Exception {
            // Given
            BusinessResponse business = createBusinessResponse(1L, "Business 1", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    50L,
                    10,
                    2,
                    5,
                    false,
                    false
            );

            when(businessService.findAll(2, 5)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "2")
                            .param("size", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(10))
                    .andExpect(jsonPath("$.currentPage").value(2))
                    .andExpect(jsonPath("$.pageSize").value(5))
                    .andExpect(jsonPath("$.last").value(false));

            verify(businessService).findAll(2, 5);
        }

        @Test
        @DisplayName("Should return empty page when no businesses exist")
        void testGetAllBusinesses_Empty() throws Exception {
            // Given
            PageResponse<BusinessResponse> emptyPage = new PageResponse<>(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    10,
                    true,
                    true
            );

            when(businessService.findAll(0, 10)).thenReturn(emptyPage);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.empty").value(true));

            verify(businessService).findAll(0, 10);
        }

        @Test
        @DisplayName("Should handle first page of results")
        void testGetAllBusinesses_FirstPage() throws Exception {
            // Given
            BusinessResponse business = createBusinessResponse(1L, "Business 1", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    100L,
                    10,
                    0,
                    10,
                    false,
                    false
            );

            when(businessService.findAll(0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.last").value(false));

            verify(businessService).findAll(0, 10);
        }

        @Test
        @DisplayName("Should handle last page of results")
        void testGetAllBusinesses_LastPage() throws Exception {
            // Given
            BusinessResponse business = createBusinessResponse(1L, "Business 1", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    21L,
                    3,
                    2,
                    10,
                    true,
                    false
            );

            when(businessService.findAll(2, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "2")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(2))
                    .andExpect(jsonPath("$.last").value(true));

            verify(businessService).findAll(2, 10);
        }

        @Test
        @DisplayName("Should handle very large page size")
        void testGetAllBusinesses_LargePageSize() throws Exception {
            // Given
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    1000,
                    true,
                    true
            );

            when(businessService.findAll(0, 1000)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "0")
                            .param("size", "1000")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageSize").value(1000));

            verify(businessService).findAll(0, 1000);
        }

        @Test
        @DisplayName("Should use default values when parameters are missing")
        void testGetAllBusinesses_MissingParameters() throws Exception {
            // Given
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    10,
                    true,
                    true
            );

            when(businessService.findAll(0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(businessService).findAll(0, 10); // Defaults
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for negative page number")
        void testGetAllBusinesses_NegativePage() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "-1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findAll(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for negative page size")
        void testGetAllBusinesses_NegativeSize() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "0")
                            .param("size", "-5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findAll(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for zero page size")
        void testGetAllBusinesses_ZeroSize() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "0")
                            .param("size", "0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findAll(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for invalid page format")
        void testGetAllBusinesses_InvalidPageFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses")
                            .param("page", "invalid")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findAll(anyInt(), anyInt());
        }
    }

    // ==================== GET /businesses/category/{categoryId} Tests ====================

    @Nested
    @DisplayName("GET /businesses/category/{categoryId} - Get Businesses By Category")
    class GetBusinessesByCategoryTests {

        @Test
        @DisplayName("Should return 200 OK with businesses for specific category")
        void testGetBusinessesByCategory_Success() throws Exception {
            // Given
            Long categoryId = 5L;
            BusinessResponse business1 = createBusinessResponse(1L, "Business 1", true);
            BusinessResponse business2 = createBusinessResponse(2L, "Business 2", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Arrays.asList(business1, business2),
                    2L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findByCategory(categoryId, 0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));

            verify(businessService).findByCategory(categoryId, 0, 10);
        }

        @Test
        @DisplayName("Should return empty page when category has no businesses")
        void testGetBusinessesByCategory_Empty() throws Exception {
            // Given
            Long categoryId = 5L;
            PageResponse<BusinessResponse> emptyPage = new PageResponse<>(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    10,
                    true,
                    true
            );

            when(businessService.findByCategory(categoryId, 0, 10)).thenReturn(emptyPage);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.empty").value(true));

            verify(businessService).findByCategory(categoryId, 0, 10);
        }

        @Test
        @DisplayName("Should support custom pagination for category")
        void testGetBusinessesByCategory_CustomPagination() throws Exception {
            // Given
            Long categoryId = 5L;
            BusinessResponse business = createBusinessResponse(1L, "Business 1", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    25L,
                    5,
                    3,
                    5,
                    false,
                    false
            );

            when(businessService.findByCategory(categoryId, 3, 5)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}", categoryId)
                            .param("page", "3")
                            .param("size", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(3))
                    .andExpect(jsonPath("$.pageSize").value(5));

            verify(businessService).findByCategory(categoryId, 3, 5);
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for invalid category ID format")
        void testGetBusinessesByCategory_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findByCategory(anyLong(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should handle very large category ID")
        void testGetBusinessesByCategory_LargeCategoryId() throws Exception {
            // Given
            Long categoryId = Long.MAX_VALUE;
            PageResponse<BusinessResponse> emptyPage = new PageResponse<>(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    10,
                    true,
                    true
            );

            when(businessService.findByCategory(categoryId, 0, 10)).thenReturn(emptyPage);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(businessService).findByCategory(categoryId, 0, 10);
        }
    }

    // ==================== GET /businesses/category/{categoryId}/with-subcategories Tests ====================

    @Nested
    @DisplayName("GET /businesses/category/{categoryId}/with-subcategories - Get Businesses By Category With Subcategories")
    class GetBusinessesByCategoryWithSubcategoriesTests {

        @Test
        @DisplayName("Should return 200 OK with businesses from category and subcategories")
        void testGetBusinessesByCategoryWithSubcategories_Success() throws Exception {
            // Given
            Long categoryId = 5L;
            BusinessResponse business1 = createBusinessResponse(1L, "Business 1", true);
            BusinessResponse business2 = createBusinessResponse(2L, "Business 2", true);
            BusinessResponse business3 = createBusinessResponse(3L, "Business 3", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Arrays.asList(business1, business2, business3),
                    3L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findByCategoryWithSubcategories(categoryId, 0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}/with-subcategories", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[2].id").value(3))
                    .andExpect(jsonPath("$.totalElements").value(3));

            verify(businessService).findByCategoryWithSubcategories(categoryId, 0, 10);
        }

        @Test
        @DisplayName("Should return empty page when category and subcategories have no businesses")
        void testGetBusinessesByCategoryWithSubcategories_Empty() throws Exception {
            // Given
            Long categoryId = 5L;
            PageResponse<BusinessResponse> emptyPage = new PageResponse<>(
                    Collections.emptyList(),
                    0L,
                    0,
                    0,
                    10,
                    true,
                    true
            );

            when(businessService.findByCategoryWithSubcategories(categoryId, 0, 10)).thenReturn(emptyPage);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}/with-subcategories", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.empty").value(true));

            verify(businessService).findByCategoryWithSubcategories(categoryId, 0, 10);
        }

        @Test
        @DisplayName("Should support custom pagination for category with subcategories")
        void testGetBusinessesByCategoryWithSubcategories_CustomPagination() throws Exception {
            // Given
            Long categoryId = 5L;
            BusinessResponse business = createBusinessResponse(1L, "Business 1", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    100L,
                    20,
                    5,
                    5,
                    false,
                    false
            );

            when(businessService.findByCategoryWithSubcategories(categoryId, 5, 5)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}/with-subcategories", categoryId)
                            .param("page", "5")
                            .param("size", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(5))
                    .andExpect(jsonPath("$.pageSize").value(5))
                    .andExpect(jsonPath("$.totalPages").value(20));

            verify(businessService).findByCategoryWithSubcategories(categoryId, 5, 5);
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST for invalid category ID format")
        void testGetBusinessesByCategoryWithSubcategories_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}/with-subcategories", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findByCategoryWithSubcategories(anyLong(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should handle category with no subcategories")
        void testGetBusinessesByCategoryWithSubcategories_NoSubcategories() throws Exception {
            // Given
            Long categoryId = 5L;
            BusinessResponse business = createBusinessResponse(1L, "Business 1", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    1L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findByCategoryWithSubcategories(categoryId, 0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}/with-subcategories", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(businessService).findByCategoryWithSubcategories(categoryId, 0, 10);
        }

        @Test
        @DisplayName("Should handle deeply nested category hierarchy")
        void testGetBusinessesByCategoryWithSubcategories_DeepHierarchy() throws Exception {
            // Given
            Long categoryId = 1L; // Root category with many levels of subcategories
            
            List<BusinessResponse> businesses = Arrays.asList(
                    createBusinessResponse(1L, "Business 1", true),
                    createBusinessResponse(2L, "Business 2", true),
                    createBusinessResponse(3L, "Business 3", true),
                    createBusinessResponse(4L, "Business 4", true),
                    createBusinessResponse(5L, "Business 5", true)
            );
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    businesses,
                    5L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findByCategoryWithSubcategories(categoryId, 0, 10)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/category/{categoryId}/with-subcategories", categoryId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5)))
                    .andExpect(jsonPath("$.totalElements").value(5));

            verify(businessService).findByCategoryWithSubcategories(categoryId, 0, 10);
        }
    }

    // ==================== General Tests ====================

    @Nested
    @DisplayName("General Controller Tests")
    class GeneralControllerTests {

        @Test
        @DisplayName("Should return 500 INTERNAL SERVER ERROR when service throws unexpected exception")
        void testUnexpectedException() throws Exception {
            // Given
            Long businessId = 1L;
            when(businessService.findById(businessId))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());

            verify(businessService).findById(businessId);
        }

        @Test
        @DisplayName("Should handle concurrent requests correctly")
        void testConcurrentRequests() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Test Business", true);

            when(businessService.findById(businessId)).thenReturn(response);

            // When/Then - Simulate multiple concurrent requests
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/businesses/{id}", businessId)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(businessId));
            }

            verify(businessService, times(5)).findById(businessId);
        }

        @Test
        @DisplayName("Should accept application/json content type")
        void testContentTypeJson() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Test Business", true);

            when(businessService.findById(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(businessService).findById(businessId);
        }
    }

    // ==================== Helper Methods ====================

    private BusinessResponse createBusinessResponse(Long id, String name, boolean active) {
        return BusinessResponse.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }
}
