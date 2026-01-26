package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.RequestCourseDto;
import dev.temnikov.qa_test.api.dto.ResponseCourseDto;
import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.entity.User;
import dev.temnikov.qa_test.security.SecurityUser;
import dev.temnikov.qa_test.service.CourseService;
import dev.temnikov.qa_test.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(
        name = "Courses",
        description = """
                Course catalog API.
                
                A course represents a type of activity (e.g. Yoga, Boxing, Strength).
                Courses are authored/owned by trainers and administered by ADMIN users.
                
                Reads require authentication (USER / TRAINER / ADMIN).
                Writes are restricted to ADMIN users.
                """
)
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "List courses (paginated)",
            description = """
                    Returns a paginated list of courses.
                    
                    Query parameters:
                    - page, size, sort (e.g. sort=name,asc)
                    
                    Access: USER / TRAINER / ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Courses returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public PageResponse<ResponseCourseDto> getAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return courseService.getAll(pageable);
    }

    @Operation(
            summary = "Get course by ID",
            description = """
                    Returns a single course by ID.
                    
                    Access: USER / TRAINER / ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @GetMapping("/{id}")
    public ResponseCourseDto getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a course (ADMIN only)",
            description = """
                    Creates a new course.
                    
                    Business rules:
                    - Only ADMIN users may create or update courses.
                    - The trainerId must reference a TRAINER user.
                      If omitted, the current authenticated user must be a TRAINER.
                    
                    Access: ADMIN only.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Course created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "422", description = "Invalid trainer or trainerId missing")
    })
    public ResponseCourseDto create(@RequestBody RequestCourseDto dto,
                            @Parameter(hidden = true)
                            @AuthenticationPrincipal SecurityUser principal) {
        User currentUser = userService.getEntityByEmail(principal.getUsername());
        return courseService.create(dto, currentUser);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a course (ADMIN only)",
            description = """
                    Updates a course.
                    
                    Business rules:
                    - Only ADMIN users may update courses.
                    - trainerId must reference a TRAINER user if provided.
                    
                    Access: ADMIN only.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseCourseDto update(@PathVariable Long id, @RequestBody ResponseCourseDto dto) {
        return courseService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a course (ADMIN only)",
            description = """
                    Deletes a course.
                    
                    Access: ADMIN only.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Course deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }
}
