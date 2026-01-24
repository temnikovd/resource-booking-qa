package dev.temnikov.qa_test.api.dto;

public record BookingDto(
        Long id,
        Long userId,
        Long slotId,
        String status
) {
}
