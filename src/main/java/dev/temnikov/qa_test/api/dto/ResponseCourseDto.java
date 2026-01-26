package dev.temnikov.qa_test.api.dto;

public record ResponseCourseDto(
        Long Id,
        String name,
        Long trainerId
) {
}
