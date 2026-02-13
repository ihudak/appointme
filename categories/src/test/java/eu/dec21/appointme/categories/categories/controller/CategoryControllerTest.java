package eu.dec21.appointme.categories.categories.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.categories.categories.service.CategoryService;
import eu.dec21.appointme.common.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // === GET /categories/{id} ===

    @Test
    void getCategoryById_existing_returns200() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Test").active(true).build();
        when(categoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void getCategoryById_notFound_returns404() throws Exception {
        when(categoryService.findById(999L)).thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }

    // === GET /categories ===

    @Test
    void getRootCategories_defaultParams_returns200() throws Exception {
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of(CategoryResponse.builder().id(1L).name("Root").build()))
                .totalElements(1).totalPages(1).pageNumber(0).pageSize(10).last(true).empty(false)
                .build();
        when(categoryService.findActiveRootCategories(0, 10)).thenReturn(page);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Root"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getRootCategories_customParams() throws Exception {
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of()).totalElements(0).totalPages(0).pageNumber(2).pageSize(5).last(true).empty(true)
                .build();
        when(categoryService.findActiveRootCategories(2, 5)).thenReturn(page);

        mockMvc.perform(get("/categories").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(true));
    }

    // === GET /categories/{parentId}/children ===

    @Test
    void getSubCategories_returns200() throws Exception {
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of(CategoryResponse.builder().id(2L).name("Sub").build()))
                .totalElements(1).totalPages(1).pageNumber(0).pageSize(10).last(true).empty(false)
                .build();
        when(categoryService.findActiveSubCategories(1L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/categories/1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sub"));
    }

    // === GET /categories/{categoryId}/subcategories/ids ===

    @Test
    void getAllSubcategoryIds_returns200() throws Exception {
        when(categoryService.findAllActiveSubcategoryIdsRecursively(1L)).thenReturn(Set.of(2L, 3L, 4L));

        mockMvc.perform(get("/categories/1/subcategories/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getAllSubcategoryIds_notFound_returns404() throws Exception {
        when(categoryService.findAllActiveSubcategoryIdsRecursively(999L))
                .thenThrow(new EntityNotFoundException("Not found"));

        mockMvc.perform(get("/categories/999/subcategories/ids"))
                .andExpect(status().isNotFound());
    }

    // === Invalid path variable type ===

    @Test
    void getCategoryById_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/categories/abc"))
                .andExpect(status().isBadRequest());
    }
}
