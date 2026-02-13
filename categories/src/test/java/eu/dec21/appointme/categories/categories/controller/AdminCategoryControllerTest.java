package eu.dec21.appointme.categories.categories.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.categories.categories.service.CategoryService;
import eu.dec21.appointme.common.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // === POST /categories/admin ===

    @Test
    void createCategory_validRequest_returns200() throws Exception {
        CategoryRequest request = new CategoryRequest(null, "New Cat", "desc", null);
        CategoryResponse response = CategoryResponse.builder().id(1L).name("New Cat").active(true).build();
        when(categoryService.save(any())).thenReturn(response);

        mockMvc.perform(post("/categories/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Cat"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        CategoryRequest request = new CategoryRequest(null, "", "desc", null);

        mockMvc.perform(post("/categories/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_nullName_returns400() throws Exception {
        CategoryRequest request = new CategoryRequest(null, null, "desc", null);

        mockMvc.perform(post("/categories/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/categories/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    // === GET /categories/admin/{id} ===

    @Test
    void getCategoryById_returns200() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Cat").build();
        when(categoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/categories/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cat"));
    }

    // === GET /categories/admin ===

    @Test
    void getRootCategories_defaultParams_returns200() throws Exception {
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of()).totalElements(0).totalPages(0).pageNumber(0).pageSize(10).last(true).empty(true)
                .build();
        when(categoryService.findAllRootCategories(0, 10, false)).thenReturn(page);

        mockMvc.perform(get("/categories/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void getRootCategories_includeInactive_returns200() throws Exception {
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of()).totalElements(0).totalPages(0).pageNumber(0).pageSize(10).last(true).empty(true)
                .build();
        when(categoryService.findAllRootCategories(0, 10, true)).thenReturn(page);

        mockMvc.perform(get("/categories/admin").param("includeInactive", "true"))
                .andExpect(status().isOk());
    }

    // === GET /categories/admin/{parentId}/children ===

    @Test
    void getSubCategories_returns200() throws Exception {
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of()).totalElements(0).totalPages(0).pageNumber(0).pageSize(10).last(true).empty(true)
                .build();
        when(categoryService.findAllSubCategories(1L, 0, 10, false)).thenReturn(page);

        mockMvc.perform(get("/categories/admin/1/children"))
                .andExpect(status().isOk());
    }

    // === GET /categories/admin/{categoryId}/subcategories/ids ===

    @Test
    void getAllSubcategoryIds_returns200() throws Exception {
        when(categoryService.findAllSubcategoryIdsRecursively(1L, false)).thenReturn(Set.of(2L));

        mockMvc.perform(get("/categories/admin/1/subcategories/ids"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllSubcategoryIds_includeInactive_returns200() throws Exception {
        when(categoryService.findAllSubcategoryIdsRecursively(1L, true)).thenReturn(Set.of(2L, 3L));

        mockMvc.perform(get("/categories/admin/1/subcategories/ids").param("includeInactive", "true"))
                .andExpect(status().isOk());
    }
}
