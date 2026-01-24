package dev.temnikov.qa_test.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Generic paginated response")
public record PageResponse<T>(

        @Schema(description = "Current page content")
        List<T> content,

        @Schema(description = "Zero-based page index")
        int page,

        @Schema(description = "Requested page size")
        int size,

        @Schema(description = "Total number of elements available")
        long totalElements,

        @Schema(description = "Total number of pages available")
        int totalPages,

        @Schema(description = "Whether this is the last page")
        boolean last
) {
}
