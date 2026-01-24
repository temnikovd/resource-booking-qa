package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.UserDto;
import dev.temnikov.qa_test.service.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration and management API")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new user",
            description = """
                Creates a new user with role USER or ADMIN.
                
                Business rules:
                - Rule 1: Creating ADMIN requires `X-Admin-Secret` header
                - Rule 2: `password` is required for any user creation
                
                Password is write-only and never returned in API responses.
                """
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
                
                Changing USER → ADMIN requires `X-Admin-Secret` (Rule 1).
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden when changing role to ADMIN"),
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
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
