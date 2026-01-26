package dev.temnikov.qa_test.api.dto;

public record ResponseBookingDto(
        Long id,
        Long userId,
        Long sessionId,
        String status
) {
}
