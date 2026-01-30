package dev.temnikov.qa_test.api.error;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        String message,
        String path
) {}
