package eu.dec21.appointme.businesses.businesses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
import eu.dec21.appointme.common.entity.Address;
import eu.dec21.appointme.common.response.PageResponse;
import eu.dec21.appointme.exceptions.OperationNotPermittedException;
import eu.dec21.appointme.exceptions.UserAuthenticationException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for OwnerBusinessController.
 * Tests all owner endpoints with authentication, authorization, validation, and error scenarios.
 * Uses @WebMvcTest for lightweight controller testing with MockMvc.
 */
@WebMvcTest(OwnerBusinessController.class)
@DisplayName("OwnerBusinessController Tests")
class OwnerBusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessService businessService;

    // ==================== POST /businesses/owner Tests ====================

    @Nested
    @DisplayName("POST /businesses/owner - Create Business")
    class CreateBusinessTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK when business is created successfully")
        void testCreateBusiness_Success() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(
                    null,
                    "New Business",
                    "A great new business",
                    null,
                    null,
                    "+491234567890",
                    "https://example.com",
                    "new@example.com"
            );

            BusinessResponse response = BusinessResponse.builder()
                    .id(1L)
                    .name("New Business")
                    .description("A great new business")
                    .phoneNumber("+491234567890")
                    .website("https://example.com")
                    .email("new@example.com")
                    .active(true)
                    .build();

            when(businessService.createBusiness(any(BusinessRequest.class), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("New Business"))
                    .andExpect(jsonPath("$.description").value("A great new business"))
                    .andExpect(jsonPath("$.phoneNumber").value("+491234567890"))
                    .andExpect(jsonPath("$.website").value("https://example.com"))
                    .andExpect(jsonPath("$.email").value("new@example.com"))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should create business with minimal required fields")
        void testCreateBusiness_MinimalFields() throws Exception {
            // Given
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

            BusinessResponse response = BusinessResponse.builder()
                    .id(1L)
                    .name("Minimal Business")
                    .active(true)
                    .build();

            when(businessService.createBusiness(any(BusinessRequest.class), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Minimal Business"));

            verify(businessService).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should create business with complete address")
        void testCreateBusiness_WithAddress() throws Exception {
            // Given
            Address address = new Address();
            address.setLine1("123 Main Street");
            address.setCity("Berlin");
            address.setPostalCode("10115");
            address.setCountryCode("DE");

            BusinessRequest request = new BusinessRequest(
                    null,
                    "Business with Address",
                    null,
                    address,
                    null,
                    null,
                    null,
                    null
            );

            BusinessResponse response = BusinessResponse.builder()
                    .id(1L)
                    .name("Business with Address")
                    .active(true)
                    .build();

            when(businessService.createBusiness(any(BusinessRequest.class), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(businessService).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when name is null")
        void testCreateBusiness_NullName() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(
                    null,
                    null, // Null name - validation should fail
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when name is blank")
        void testCreateBusiness_BlankName() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(
                    null,
                    "   ", // Blank name - validation should fail
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when name is empty string")
        void testCreateBusiness_EmptyName() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(
                    null,
                    "", // Empty name - validation should fail
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when request body is missing")
        void testCreateBusiness_MissingBody() throws Exception {
            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when JSON is malformed")
        void testCreateBusiness_MalformedJson() throws Exception {
            // Given
            String malformedJson = "{ invalid json }";

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testCreateBusiness_Unauthorized() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing")
        void testCreateBusiness_MissingCsrfToken() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then - No .with(csrf())
            mockMvc.perform(post("/businesses/owner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).createBusiness(any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 401 UNAUTHORIZED when authentication fails")
        void testCreateBusiness_AuthenticationFails() throws Exception {
            // Given
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            when(businessService.createBusiness(any(BusinessRequest.class), any()))
                    .thenThrow(new UserAuthenticationException("User not authenticated"));

            // When/Then
            mockMvc.perform(post("/businesses/owner")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService).createBusiness(any(BusinessRequest.class), any());
        }
    }

    // ==================== GET /businesses/owner Tests ====================

    @Nested
    @DisplayName("GET /businesses/owner - Get My Businesses")
    class GetMyBusinessesTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK with owner's businesses")
        void testGetMyBusinesses_Success() throws Exception {
            // Given
            BusinessResponse business1 = createBusinessResponse(1L, "My Business 1", true);
            BusinessResponse business2 = createBusinessResponse(2L, "My Business 2", false);

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

            when(businessService.findByOwner(any(), eq(0), eq(10))).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("My Business 1"))
                    .andExpect(jsonPath("$.content[0].active").value(true))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].name").value("My Business 2"))
                    .andExpect(jsonPath("$.content[1].active").value(false))
                    .andExpect(jsonPath("$.totalElements").value(2));

            verify(businessService).findByOwner(any(), eq(0), eq(10));
        }

        @Test
        @WithMockUser
        @DisplayName("Should include inactive businesses in owner's list")
        void testGetMyBusinesses_IncludesInactive() throws Exception {
            // Given
            BusinessResponse inactiveBusiness = createBusinessResponse(1L, "Inactive Business", false);

            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(inactiveBusiness),
                    1L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findByOwner(any(), eq(0), eq(10))).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].active").value(false));

            verify(businessService).findByOwner(any(), eq(0), eq(10));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty page when owner has no businesses")
        void testGetMyBusinesses_Empty() throws Exception {
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

            when(businessService.findByOwner(any(), eq(0), eq(10))).thenReturn(emptyPage);

            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.empty").value(true));

            verify(businessService).findByOwner(any(), eq(0), eq(10));
        }

        @Test
        @WithMockUser
        @DisplayName("Should support custom pagination")
        void testGetMyBusinesses_CustomPagination() throws Exception {
            // Given
            BusinessResponse business = createBusinessResponse(1L, "Business", true);

            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(business),
                    50L,
                    10,
                    3,
                    5,
                    false,
                    false
            );

            when(businessService.findByOwner(any(), eq(3), eq(5))).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .param("page", "3")
                            .param("size", "5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(3))
                    .andExpect(jsonPath("$.pageSize").value(5))
                    .andExpect(jsonPath("$.totalPages").value(10));

            verify(businessService).findByOwner(any(), eq(3), eq(5));
        }

        @Test
        @WithMockUser
        @DisplayName("Should use default pagination when parameters not provided")
        void testGetMyBusinesses_DefaultPagination() throws Exception {
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

            when(businessService.findByOwner(any(), eq(0), eq(10))).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(businessService).findByOwner(any(), eq(0), eq(10)); // Defaults
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testGetMyBusinesses_Unauthorized() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).findByOwner(any(), anyInt(), anyInt());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST for negative page number")
        void testGetMyBusinesses_NegativePage() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .param("page", "-1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findByOwner(any(), anyInt(), anyInt());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST for zero page size")
        void testGetMyBusinesses_ZeroSize() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/owner")
                            .param("page", "0")
                            .param("size", "0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findByOwner(any(), anyInt(), anyInt());
        }
    }

    // ==================== GET /businesses/owner/{id} Tests ====================

    @Nested
    @DisplayName("GET /businesses/owner/{id} - Get My Business By ID")
    class GetMyBusinessByIdTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK with business when owned by user")
        void testGetMyBusinessById_Success() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "My Business", true);

            when(businessService.findByIdAndOwner(eq(businessId), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("My Business"))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).findByIdAndOwner(eq(businessId), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return inactive business if owned by user")
        void testGetMyBusinessById_Inactive() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Inactive Business", false);

            when(businessService.findByIdAndOwner(eq(businessId), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));

            verify(businessService).findByIdAndOwner(eq(businessId), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 NOT FOUND when business not owned by user")
        void testGetMyBusinessById_NotOwner() throws Exception {
            // Given
            Long businessId = 999L;

            when(businessService.findByIdAndOwner(eq(businessId), any()))
                    .thenThrow(new EntityNotFoundException("Business not found with id " + businessId + " for current owner"));

            // When/Then
            mockMvc.perform(get("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).findByIdAndOwner(eq(businessId), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 NOT FOUND when business does not exist")
        void testGetMyBusinessById_NotFound() throws Exception {
            // Given
            Long businessId = 999L;

            when(businessService.findByIdAndOwner(eq(businessId), any()))
                    .thenThrow(new EntityNotFoundException("Business not found"));

            // When/Then
            mockMvc.perform(get("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).findByIdAndOwner(eq(businessId), any());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testGetMyBusinessById_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(get("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).findByIdAndOwner(anyLong(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testGetMyBusinessById_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/owner/{id}", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findByIdAndOwner(anyLong(), any());
        }
    }

    // ==================== PUT /businesses/owner/{id} Tests ====================

    @Nested
    @DisplayName("PUT /businesses/owner/{id} - Update My Business")
    class UpdateMyBusinessTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK when business is updated successfully")
        void testUpdateMyBusiness_Success() throws Exception {
            // Given
            Long businessId = 1L;

            BusinessRequest request = new BusinessRequest(
                    null,
                    "Updated Business",
                    "Updated description",
                    null,
                    null,
                    "+491234567890",
                    "https://new-website.com",
                    "updated@example.com"
            );

            BusinessResponse response = BusinessResponse.builder()
                    .id(businessId)
                    .name("Updated Business")
                    .description("Updated description")
                    .phoneNumber("+491234567890")
                    .website("https://new-website.com")
                    .email("updated@example.com")
                    .active(true)
                    .build();

            when(businessService.updateBusinessByOwner(eq(businessId), any(BusinessRequest.class), any()))
                    .thenReturn(response);

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("Updated Business"))
                    .andExpect(jsonPath("$.description").value("Updated description"))
                    .andExpect(jsonPath("$.phoneNumber").value("+491234567890"))
                    .andExpect(jsonPath("$.website").value("https://new-website.com"))
                    .andExpect(jsonPath("$.email").value("updated@example.com"));

            verify(businessService).updateBusinessByOwner(eq(businessId), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should support partial update with only some fields")
        void testUpdateMyBusiness_PartialUpdate() throws Exception {
            // Given
            Long businessId = 1L;

            BusinessRequest request = new BusinessRequest(
                    null,
                    "Updated Name",
                    null, // Not updating description
                    null,
                    null,
                    null,
                    null,
                    null  // Not updating email
            );

            BusinessResponse response = createBusinessResponse(businessId, "Updated Name", true);

            when(businessService.updateBusinessByOwner(eq(businessId), any(BusinessRequest.class), any()))
                    .thenReturn(response);

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"));

            verify(businessService).updateBusinessByOwner(eq(businessId), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when user is not owner")
        void testUpdateMyBusiness_NotOwner() throws Exception {
            // Given
            Long businessId = 1L;

            BusinessRequest request = new BusinessRequest(
                    null,
                    "Updated Business",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(businessService.updateBusinessByOwner(eq(businessId), any(BusinessRequest.class), any()))
                    .thenThrow(new OperationNotPermittedException("You do not have permission to update this business"));

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService).updateBusinessByOwner(eq(businessId), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when name is null")
        void testUpdateMyBusiness_NullName() throws Exception {
            // Given
            Long businessId = 1L;

            BusinessRequest request = new BusinessRequest(
                    null,
                    null, // Null name - validation should fail
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByOwner(anyLong(), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when name is blank")
        void testUpdateMyBusiness_BlankName() throws Exception {
            // Given
            Long businessId = 1L;

            BusinessRequest request = new BusinessRequest(
                    null,
                    "   ", // Blank name - validation should fail
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByOwner(anyLong(), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when request body is missing")
        void testUpdateMyBusiness_MissingBody() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByOwner(anyLong(), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when JSON is malformed")
        void testUpdateMyBusiness_MalformedJson() throws Exception {
            // Given
            Long businessId = 1L;
            String malformedJson = "{ invalid json }";

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByOwner(anyLong(), any(BusinessRequest.class), any());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testUpdateMyBusiness_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).updateBusinessByOwner(anyLong(), any(BusinessRequest.class), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing")
        void testUpdateMyBusiness_MissingCsrfToken() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then - No .with(csrf())
            mockMvc.perform(put("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).updateBusinessByOwner(anyLong(), any(BusinessRequest.class), any());
        }
    }

    // ==================== PATCH /businesses/owner/{id}/active Tests ====================

    @Nested
    @DisplayName("PATCH /businesses/owner/{id}/active - Toggle Business Active Status")
    class ToggleBusinessActiveTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK when activating business")
        void testToggleActive_Activate() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Business", true);

            when(businessService.toggleBusinessActiveByOwner(eq(businessId), eq(true), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).toggleBusinessActiveByOwner(eq(businessId), eq(true), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK when deactivating business")
        void testToggleActive_Deactivate() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Business", false);

            when(businessService.toggleBusinessActiveByOwner(eq(businessId), eq(false), any())).thenReturn(response);

            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.active").value(false));

            verify(businessService).toggleBusinessActiveByOwner(eq(businessId), eq(false), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when user is not owner")
        void testToggleActive_NotOwner() throws Exception {
            // Given
            Long businessId = 1L;

            when(businessService.toggleBusinessActiveByOwner(eq(businessId), eq(true), any()))
                    .thenThrow(new OperationNotPermittedException("You do not have permission to modify this business"));

            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService).toggleBusinessActiveByOwner(eq(businessId), eq(true), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when active parameter is missing")
        void testToggleActive_MissingParameter() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).toggleBusinessActiveByOwner(anyLong(), anyBoolean(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST when active parameter has invalid format")
        void testToggleActive_InvalidParameter() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).toggleBusinessActiveByOwner(anyLong(), anyBoolean(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testToggleActive_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", "invalid")
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).toggleBusinessActiveByOwner(anyLong(), anyBoolean(), any());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testToggleActive_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).toggleBusinessActiveByOwner(anyLong(), anyBoolean(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing")
        void testToggleActive_MissingCsrfToken() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then - No .with(csrf())
            mockMvc.perform(patch("/businesses/owner/{id}/active", businessId)
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).toggleBusinessActiveByOwner(anyLong(), anyBoolean(), any());
        }
    }

    // ==================== DELETE /businesses/owner/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /businesses/owner/{id} - Delete My Business")
    class DeleteMyBusinessTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 204 NO CONTENT when business is deleted successfully")
        void testDeleteMyBusiness_Success() throws Exception {
            // Given
            Long businessId = 1L;

            doNothing().when(businessService).deleteBusinessByOwner(eq(businessId), any());

            // When/Then
            mockMvc.perform(delete("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(businessService).deleteBusinessByOwner(eq(businessId), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when user is not owner")
        void testDeleteMyBusiness_NotOwner() throws Exception {
            // Given
            Long businessId = 1L;

            doThrow(new OperationNotPermittedException("You do not have permission to delete this business"))
                    .when(businessService).deleteBusinessByOwner(eq(businessId), any());

            // When/Then
            mockMvc.perform(delete("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService).deleteBusinessByOwner(eq(businessId), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testDeleteMyBusiness_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(delete("/businesses/owner/{id}", "invalid")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).deleteBusinessByOwner(anyLong(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should delete inactive business if owned by user")
        void testDeleteMyBusiness_InactiveBusiness() throws Exception {
            // Given
            Long businessId = 2L;

            doNothing().when(businessService).deleteBusinessByOwner(eq(businessId), any());

            // When/Then
            mockMvc.perform(delete("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(businessService).deleteBusinessByOwner(eq(businessId), any());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testDeleteMyBusiness_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(delete("/businesses/owner/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).deleteBusinessByOwner(anyLong(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing")
        void testDeleteMyBusiness_MissingCsrfToken() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then - No .with(csrf())
            mockMvc.perform(delete("/businesses/owner/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).deleteBusinessByOwner(anyLong(), any());
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
