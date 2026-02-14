package eu.dec21.appointme.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Generic paginated response wrapper containing a list of items and pagination metadata")
public class PageResponse<T> {

    @Schema(description = "List of items in the current page")
    private List<T> content;
    
    @Schema(description = "Total number of items across all pages", example = "127")
    private long totalElements;
    
    @Schema(description = "Total number of pages available", example = "13")
    private int totalPages;
    
    @Schema(description = "Current page number (zero-based)", example = "0")
    private int pageNumber;
    
    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
    
    @Schema(description = "Whether this page is empty (no content)", example = "false")
    private boolean empty;

    @Schema(description = "Whether this is the first page", example = "true")
    public boolean isFirst() {
        return pageNumber == 0;
    }
}
