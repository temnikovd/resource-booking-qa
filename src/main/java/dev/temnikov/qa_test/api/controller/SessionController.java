package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.RequestSessionDto;
import dev.temnikov.qa_test.api.dto.ResponseSessionDto;
import dev.temnikov.qa_test.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import dev.temnikov.qa_test.api.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(
        name = "Sessions",
        description = """
                Sessions represent scheduled occurrences of a course.

                A session defines:
                - which course is being run,
                - when it starts and ends,
                - which trainer leads it (inherited from the course, if applicable),
                - (future) how many participants may join (capacity).

                Reads require authentication (USER / TRAINER / ADMIN).
                Writes are restricted to ADMIN users.
                """
)
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(
            summary = "List sessions (paginated)",
            description = """
                    Returns a paginated list of sessions.

                    Query parameters:
                    - page, size, sort (e.g. sort=startTime,asc)

                    Access: USER / TRAINER / ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessions returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public PageResponse<ResponseSessionDto> getAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return sessionService.getAll(pageable);
    }

    @Operation(
            summary = "Get session by ID",
            description = """
                    Returns a single session by ID.

                    Access: USER / TRAINER / ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/{id}")
    public ResponseSessionDto getById(@PathVariable Long id) {
        return sessionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a session (ADMIN only)",
            description = """
                    Creates a new scheduled session for a course.

                    Business rules:
                    - Sessions must start in the future.
                    - Sessions must not overlap for the same course.
                    - Course must exist.
                    - Capacity is optional and must be >= 1 if provided. Default value is 5

                    Access: ADMIN only.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session created"),
            @ApiResponse(responseCode = "400", description = "Validation error (past start or overlapping session, or negative capacity)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "422", description = "Invalid course or invalid capacity")
    })
    public ResponseSessionDto create(@RequestBody RequestSessionDto dto) {
        return sessionService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a session (ADMIN only)",
            description = """
                    Updates a session.

                    Same validation rules as create:
                    - future-only
                    - non-overlapping
                    - valid course reference

                    Access: ADMIN only.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session updated"),
            @ApiResponse(responseCode = "400", description = "Validation error (past start or overlapping session)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseSessionDto update(@PathVariable Long id, @RequestBody RequestSessionDto dto) {
        return sessionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a session (ADMIN only)",
            description = """
                    Deletes a session by ID.

                    Access: ADMIN only.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }
}
