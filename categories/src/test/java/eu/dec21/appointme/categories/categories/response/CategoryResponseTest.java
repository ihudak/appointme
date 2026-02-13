package eu.dec21.appointme.categories.categories.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResponseTest {

    @Test
    void builder_allFields() {
        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Test")
                .description("desc")
                .active(true)
                .parentId(2L)
                .imageUrl("http://img.png")
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test");
        assertThat(response.getDescription()).isEqualTo("desc");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getParentId()).isEqualTo(2L);
        assertThat(response.getImageUrl()).isEqualTo("http://img.png");
    }

    @Test
    void builder_defaults() {
        CategoryResponse response = CategoryResponse.builder().build();
        assertThat(response.getId()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.getDescription()).isNull();
        assertThat(response.getParentId()).isNull();
        assertThat(response.getImageUrl()).isNull();
        assertThat(response.isActive()).isFalse();
    }

    @Test
    void setters() {
        CategoryResponse response = new CategoryResponse();
        response.setId(5L);
        response.setName("Updated");
        response.setDescription("new desc");
        response.setParentId(3L);
        response.setActive(true);
        response.setImageUrl("url");

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("Updated");
        assertThat(response.getDescription()).isEqualTo("new desc");
        assertThat(response.getParentId()).isEqualTo(3L);
        assertThat(response.isActive()).isTrue();
        assertThat(response.getImageUrl()).isEqualTo("url");
    }

    @Test
    void allArgsConstructor() {
        CategoryResponse response = new CategoryResponse(1L, "Name", "desc", 2L, "img", true);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Name");
        assertThat(response.getParentId()).isEqualTo(2L);
    }

    @Test
    void builder_noParentId_isNull() {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Root").build();
        assertThat(response.getParentId()).isNull();
    }
}
