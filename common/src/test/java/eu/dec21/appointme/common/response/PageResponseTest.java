package eu.dec21.appointme.common.response;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void builder_withAllFields() {
        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("a", "b", "c"))
                .totalElements(100)
                .totalPages(10)
                .pageNumber(2)
                .pageSize(10)
                .last(false)
                .empty(false)
                .build();

        assertThat(response.getContent()).containsExactly("a", "b", "c");
        assertThat(response.getTotalElements()).isEqualTo(100);
        assertThat(response.getTotalPages()).isEqualTo(10);
        assertThat(response.getPageNumber()).isEqualTo(2);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.isLast()).isFalse();
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void noArgsConstructor() {
        PageResponse<String> response = new PageResponse<>();

        assertThat(response.getContent()).isNull();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(0);
        assertThat(response.isLast()).isFalse();
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void isFirst_whenPageNumberIsZero() {
        PageResponse<String> response = PageResponse.<String>builder()
                .pageNumber(0)
                .build();

        assertThat(response.isFirst()).isTrue();
    }

    @Test
    void isFirst_whenPageNumberIsNotZero() {
        PageResponse<String> response = PageResponse.<String>builder()
                .pageNumber(1)
                .build();

        assertThat(response.isFirst()).isFalse();
    }

    @Test
    void isFirst_whenPageNumberIsNegative() {
        PageResponse<String> response = PageResponse.<String>builder()
                .pageNumber(-1)
                .build();

        assertThat(response.isFirst()).isFalse();
    }

    @Test
    void emptyPage() {
        PageResponse<String> response = PageResponse.<String>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .pageNumber(0)
                .pageSize(10)
                .last(true)
                .empty(true)
                .build();

        assertThat(response.getContent()).isEmpty();
        assertThat(response.isEmpty()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.isFirst()).isTrue();
    }

    @Test
    void lastPage() {
        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("item"))
                .totalElements(51)
                .totalPages(6)
                .pageNumber(5)
                .pageSize(10)
                .last(true)
                .empty(false)
                .build();

        assertThat(response.isLast()).isTrue();
        assertThat(response.isFirst()).isFalse();
    }

    @Test
    void setters() {
        PageResponse<Integer> response = new PageResponse<>();
        response.setContent(List.of(1, 2, 3));
        response.setTotalElements(50);
        response.setTotalPages(5);
        response.setPageNumber(0);
        response.setPageSize(10);
        response.setLast(false);
        response.setEmpty(false);

        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(50);
        assertThat(response.isFirst()).isTrue();
    }

    @Test
    void genericType_worksWithDifferentTypes() {
        PageResponse<Long> longPage = PageResponse.<Long>builder()
                .content(List.of(1L, 2L))
                .build();

        assertThat(longPage.getContent()).containsExactly(1L, 2L);
    }

    @Test
    void singleItemPage() {
        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("only-item"))
                .totalElements(1)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(10)
                .last(true)
                .empty(false)
                .build();

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.getTotalElements()).isEqualTo(1);
    }
}
