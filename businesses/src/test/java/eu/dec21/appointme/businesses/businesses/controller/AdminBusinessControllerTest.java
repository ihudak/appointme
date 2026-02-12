package eu.dec21.appointme.businesses.businesses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
import eu.dec21.appointme.common.entity.Address;
import eu.dec21.appointme.common.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
 * Comprehensive unit tests for AdminBusinessController.
 * Tests all admin endpoints with security, validation, and error scenarios.
 * Uses @WebMvcTest for lightweight controller testing with MockMvc.
 */
@WebMvcTest(AdminBusinessController.class)
@DisplayName("AdminBusinessController Tests")
class AdminBusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @MockitoBean
    private BusinessService businessService;

    // ==================== GET /businesses/admin Tests ====================

    @Nested
    @DisplayName("GET /businesses/admin - Get All Businesses")
    class GetAllBusinessesTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK with all businesses including inactive when includeInactive=true")
        void testGetAllBusinesses_IncludeInactive() throws Exception {
            // Given
            BusinessResponse activeBusiness = createBusinessResponse(1L, "Active Business", true);
            BusinessResponse inactiveBusiness = createBusinessResponse(2L, "Inactive Business", false);
            
            List<BusinessResponse> businesses = Arrays.asList(activeBusiness, inactiveBusiness);
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    businesses,
                    2L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findAllBusinesses(0, 10, true)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .param("includeInactive", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Active Business"))
                    .andExpect(jsonPath("$.content[0].active").value(true))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].name").value("Inactive Business"))
                    .andExpect(jsonPath("$.content[1].active").value(false))
                    .andExpect(jsonPath("$.totalElements").value(2));

            verify(businessService).findAllBusinesses(0, 10, true);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK with only active businesses when includeInactive=false")
        void testGetAllBusinesses_ExcludeInactive() throws Exception {
            // Given
            BusinessResponse activeBusiness = createBusinessResponse(1L, "Active Business", true);
            
            PageResponse<BusinessResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(activeBusiness),
                    1L,
                    1,
                    0,
                    10,
                    true,
                    false
            );

            when(businessService.findAllBusinesses(0, 10, false)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .param("includeInactive", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].active").value(true));

            verify(businessService).findAllBusinesses(0, 10, false);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should use default includeInactive=false when parameter not provided")
        void testGetAllBusinesses_DefaultIncludeInactive() throws Exception {
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

            when(businessService.findAllBusinesses(0, 10, false)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(businessService).findAllBusinesses(0, 10, false); // Default false
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should support custom pagination")
        void testGetAllBusinesses_CustomPagination() throws Exception {
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

            when(businessService.findAllBusinesses(3, 5, true)).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .param("page", "3")
                            .param("size", "5")
                            .param("includeInactive", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageNumber").value(3))
                    .andExpect(jsonPath("$.pageSize").value(5))
                    .andExpect(jsonPath("$.totalPages").value(10));

            verify(businessService).findAllBusinesses(3, 5, true);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
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

            when(businessService.findAllBusinesses(0, 10, true)).thenReturn(emptyPage);

            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .param("includeInactive", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.empty").value(true));

            verify(businessService).findAllBusinesses(0, 10, true);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 FORBIDDEN when user is not ADMIN")
        void testGetAllBusinesses_Forbidden() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).findAllBusinesses(anyInt(), anyInt(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testGetAllBusinesses_Unauthorized() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).findAllBusinesses(anyInt(), anyInt(), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST for negative page number")
        void testGetAllBusinesses_NegativePage() throws Exception {
            // When/Then - @Min(0) validation rejects negative page
            mockMvc.perform(get("/businesses/admin")
                            .param("page", "-1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findAllBusinesses(anyInt(), anyInt(), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST for invalid includeInactive format")
        void testGetAllBusinesses_InvalidIncludeInactive() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/admin")
                            .param("includeInactive", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findAllBusinesses(anyInt(), anyInt(), anyBoolean());
        }
    }

    // ==================== GET /businesses/admin/{id} Tests ====================

    @Nested
    @DisplayName("GET /businesses/admin/{id} - Get Business By ID")
    class GetBusinessByIdTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK with active business")
        void testGetBusinessById_Active() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Active Business", true);

            when(businessService.findByIdAdmin(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("Active Business"))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).findByIdAdmin(businessId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK with inactive business")
        void testGetBusinessById_Inactive() throws Exception {
            // Given
            Long businessId = 2L;
            BusinessResponse response = createBusinessResponse(businessId, "Inactive Business", false);

            when(businessService.findByIdAdmin(businessId)).thenReturn(response);

            // When/Then
            mockMvc.perform(get("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.name").value("Inactive Business"))
                    .andExpect(jsonPath("$.active").value(false));

            verify(businessService).findByIdAdmin(businessId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 NOT FOUND when business does not exist")
        void testGetBusinessById_NotFound() throws Exception {
            // Given
            Long businessId = 999L;

            when(businessService.findByIdAdmin(businessId))
                    .thenThrow(new EntityNotFoundException("Business not found with id " + businessId));

            // When/Then
            mockMvc.perform(get("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).findByIdAdmin(businessId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 FORBIDDEN when user is not ADMIN")
        void testGetBusinessById_Forbidden() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(get("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).findByIdAdmin(anyLong());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testGetBusinessById_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(get("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).findByIdAdmin(anyLong());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testGetBusinessById_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(get("/businesses/admin/{id}", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).findByIdAdmin(anyLong());
        }
    }

    // ==================== PUT /businesses/admin/{id} Tests ====================

    @Nested
    @DisplayName("PUT /businesses/admin/{id} - Update Business")
    class UpdateBusinessTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK when business is updated successfully")
        void testUpdateBusiness_Success() throws Exception {
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

            when(businessService.updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class)))
                    .thenReturn(response);

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
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

            verify(businessService).updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK for partial update")
        void testUpdateBusiness_PartialUpdate() throws Exception {
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

            when(businessService.updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class)))
                    .thenReturn(response);

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"));

            verify(businessService).updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 NOT FOUND when business does not exist")
        void testUpdateBusiness_NotFound() throws Exception {
            // Given
            Long businessId = 999L;
            
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

            when(businessService.updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class)))
                    .thenThrow(new EntityNotFoundException("Business not found with id " + businessId));

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST when name is null")
        void testUpdateBusiness_NullName() throws Exception {
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
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST when name is blank")
        void testUpdateBusiness_BlankName() throws Exception {
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
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST when request body is missing")
        void testUpdateBusiness_MissingBody() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST when JSON is malformed")
        void testUpdateBusiness_MalformedJson() throws Exception {
            // Given
            Long businessId = 1L;
            String malformedJson = "{ invalid json }";

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle business with complete address")
        void testUpdateBusiness_WithAddress() throws Exception {
            // Given
            Long businessId = 1L;
            
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
            
            BusinessResponse response = createBusinessResponse(businessId, "Business with Address", true);

            when(businessService.updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class)))
                    .thenReturn(response);

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(businessService).updateBusinessByAdmin(eq(businessId), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 FORBIDDEN when user is not ADMIN")
        void testUpdateBusiness_Forbidden() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testUpdateBusiness_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }
    }

    // ==================== PATCH /businesses/admin/{id}/active Tests ====================

    @Nested
    @DisplayName("PATCH /businesses/admin/{id}/active - Toggle Business Active Status")
    class ToggleBusinessActiveTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK when activating business")
        void testToggleActive_Activate() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Business", true);

            when(businessService.toggleBusinessActiveByAdmin(businessId, true)).thenReturn(response);

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.active").value(true));

            verify(businessService).toggleBusinessActiveByAdmin(businessId, true);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 OK when deactivating business")
        void testToggleActive_Deactivate() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessResponse response = createBusinessResponse(businessId, "Business", false);

            when(businessService.toggleBusinessActiveByAdmin(businessId, false)).thenReturn(response);

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(businessId))
                    .andExpect(jsonPath("$.active").value(false));

            verify(businessService).toggleBusinessActiveByAdmin(businessId, false);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 NOT FOUND when business does not exist")
        void testToggleActive_NotFound() throws Exception {
            // Given
            Long businessId = 999L;

            when(businessService.toggleBusinessActiveByAdmin(businessId, true))
                    .thenThrow(new EntityNotFoundException("Business not found with id " + businessId));

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).toggleBusinessActiveByAdmin(businessId, true);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST when active parameter is missing")
        void testToggleActive_MissingParameter() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).toggleBusinessActiveByAdmin(anyLong(), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST when active parameter has invalid format")
        void testToggleActive_InvalidParameter() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).toggleBusinessActiveByAdmin(anyLong(), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testToggleActive_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", "invalid")
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).toggleBusinessActiveByAdmin(anyLong(), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 FORBIDDEN when user is not ADMIN")
        void testToggleActive_Forbidden() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).toggleBusinessActiveByAdmin(anyLong(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testToggleActive_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .with(csrf())
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).toggleBusinessActiveByAdmin(anyLong(), anyBoolean());
        }
    }

    // ==================== DELETE /businesses/admin/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /businesses/admin/{id} - Delete Business")
    class DeleteBusinessTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 204 NO CONTENT when business is deleted successfully")
        void testDeleteBusiness_Success() throws Exception {
            // Given
            Long businessId = 1L;

            doNothing().when(businessService).deleteBusinessByAdmin(businessId);

            // When/Then
            mockMvc.perform(delete("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(businessService).deleteBusinessByAdmin(businessId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 NOT FOUND when business does not exist")
        void testDeleteBusiness_NotFound() throws Exception {
            // Given
            Long businessId = 999L;

            doThrow(new EntityNotFoundException("Business not found with id " + businessId))
                    .when(businessService).deleteBusinessByAdmin(businessId);

            // When/Then
            mockMvc.perform(delete("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(businessService).deleteBusinessByAdmin(businessId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 BAD REQUEST for invalid ID format")
        void testDeleteBusiness_InvalidIdFormat() throws Exception {
            // When/Then
            mockMvc.perform(delete("/businesses/admin/{id}", "invalid")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(businessService, never()).deleteBusinessByAdmin(anyLong());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle deletion of inactive business")
        void testDeleteBusiness_InactiveBusiness() throws Exception {
            // Given
            Long businessId = 2L;

            doNothing().when(businessService).deleteBusinessByAdmin(businessId);

            // When/Then
            mockMvc.perform(delete("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(businessService).deleteBusinessByAdmin(businessId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 FORBIDDEN when user is not ADMIN")
        void testDeleteBusiness_Forbidden() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(delete("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).deleteBusinessByAdmin(anyLong());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when user not authenticated")
        void testDeleteBusiness_Unauthorized() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then
            mockMvc.perform(delete("/businesses/admin/{id}", businessId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(businessService, never()).deleteBusinessByAdmin(anyLong());
        }
    }

    // ==================== CSRF Protection Tests ====================

    @Nested
    @DisplayName("CSRF Protection Tests")
    class CsrfProtectionTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing for PUT request")
        void testUpdateBusiness_MissingCsrfToken() throws Exception {
            // Given
            Long businessId = 1L;
            BusinessRequest request = new BusinessRequest(null, "Business", null, null, null, null, null, null);

            // When/Then - No .with(csrf())
            mockMvc.perform(put("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).updateBusinessByAdmin(anyLong(), any(BusinessRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing for PATCH request")
        void testToggleActive_MissingCsrfToken() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then - No .with(csrf())
            mockMvc.perform(patch("/businesses/admin/{id}/active", businessId)
                            .param("active", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).toggleBusinessActiveByAdmin(anyLong(), anyBoolean());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 403 FORBIDDEN when CSRF token is missing for DELETE request")
        void testDeleteBusiness_MissingCsrfToken() throws Exception {
            // Given
            Long businessId = 1L;

            // When/Then - No .with(csrf())
            mockMvc.perform(delete("/businesses/admin/{id}", businessId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(businessService, never()).deleteBusinessByAdmin(anyLong());
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
