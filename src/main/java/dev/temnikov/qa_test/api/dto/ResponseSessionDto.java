package dev.temnikov.qa_test.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ResponseSessionDto(
        Long id,
        Long courseId,
        @Schema(example = "2026-01-24T10:00") LocalDateTime startTime,
        @Schema(example = "2026-01-24T11:00") LocalDateTime endTime,
        int capacity,
        int currentBookings
) {
}
