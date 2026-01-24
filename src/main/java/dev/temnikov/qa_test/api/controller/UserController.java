package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.UserDto;
import dev.temnikov.qa_test.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "Users",
        description = """
                User registration and management API.
                
                - Public registration endpoint (POST /api/users).
                - All read/update/delete operations require authentication.
                - ADMIN user creation and updates are guarded by an admin secret (Rule 1 in RULES.md).
                """
)
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = """
                    Returns all users.
                    
                    Requires authentication (USER or ADMIN).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by id",
            description = """
                    Returns a single user by id.
                    
                    Requires authentication (USER or ADMIN).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new user",
            description = """
                Registers a new user with role USER or ADMIN.
                
                Business rules:
                - Rule 1: Creating ADMIN requires `X-Admin-Secret` header with a valid secret.
                - Rule 2: `password` is required for any user creation.
                
                This endpoint is intentionally left unauthenticated to allow open registration flows.
                Password is write-only and never returned in API responses.
                """,
            security = {} // override global security, registration is public
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Password missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden when creating ADMIN without valid secret")
    })
    @Parameter(name = "X-Admin-Secret", description = "Required only when creating or updating an ADMIN user", required = false)
    public UserDto create(@RequestBody UserDto dto,
                          @RequestHeader(value = "X-Admin-Secret", required = false) String adminSecret) {
        return userService.create(dto, adminSecret);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update existing user",
            description = """
                Updates user fields. Password change is optional.
                
                Changing role from USER to ADMIN requires `X-Admin-Secret` (Rule 1).
                Requires authentication.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden when changing role to ADMIN without valid secret"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameter(name = "X-Admin-Secret", description = "Required only when creating or updating an ADMIN user", required = false)
    public UserDto update(@PathVariable Long id,
                          @RequestBody UserDto dto,
                          @RequestHeader(value = "X-Admin-Secret", required = false) String adminSecret) {
        return userService.update(id, dto, adminSecret);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete user",
            description = """
                    Deletes a user by id.
                    
                    Requires authentication. In typical scenarios this operation is expected
                    to be performed by ADMIN users or in test setups.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
