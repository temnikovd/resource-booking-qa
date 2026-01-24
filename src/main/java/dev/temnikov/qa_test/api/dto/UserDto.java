package dev.temnikov.qa_test.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User DTO")
public record UserDto(
        @Schema(description = "User id")
        Long id,

        @Schema(description = "Email used to authenticate")
        String email,

        @Schema(description = "Full name of the user")
        String fullName,

        @Schema(description = "Role assigned to the user (USER or ADMIN)")
        String role,

        @Schema(description = "Password (write-only)", accessMode = Schema.AccessMode.WRITE_ONLY)
        String password
) {
}
