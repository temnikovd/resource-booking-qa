package dev.temnikov.qa_test.api.dto;

public record RequestBookingDto(
        Long userId,
        Long sessionId,
        String status
) {
}
