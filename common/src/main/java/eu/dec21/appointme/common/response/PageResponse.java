package eu.dec21.appointme.common.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
    private boolean last;
    private boolean empty;

    public boolean isFirst() {
        return pageNumber == 0;
    }
}
