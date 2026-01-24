package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.ResourceDto;
import dev.temnikov.qa_test.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(
        name = "Resources",
        description = """
                Bookable resources (for example rooms, courts, desks).
                
                Reads require authentication (USER or ADMIN).
                Writes are restricted to ADMIN users (Rule 3 in RULES.md).
                """
)
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(
            summary = "Get all resources",
            description = "Returns all resources. Requires authentication (USER or ADMIN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resources returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public List<ResourceDto> getAll() {
        return resourceService.getAll();
    }

    @Operation(
            summary = "Get resource by id",
            description = "Returns a single resource by id. Requires authentication (USER or ADMIN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    @GetMapping("/{id}")
    public ResourceDto getById(@PathVariable Long id) {
        return resourceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new resource (ADMIN only)",
            description = """
                    Creates a new bookable resource.
                    
                    Requires authentication and ADMIN role.
                    Business rule:
                    - Rule 3: Only ADMIN can create, update or delete resources.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resource created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User lacks ADMIN role")
    })
    public ResourceDto create(@RequestBody ResourceDto dto) {
        return resourceService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing resource (ADMIN only)",
            description = """
                    Updates fields of an existing resource.
                    
                    Requires authentication and ADMIN role (Rule 3).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User lacks ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResourceDto update(@PathVariable Long id, @RequestBody ResourceDto dto) {
        return resourceService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a resource (ADMIN only)",
            description = """
                    Deletes a resource by id.
                    
                    Requires authentication and ADMIN role (Rule 3).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User lacks ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public void delete(@PathVariable Long id) {
        resourceService.delete(id);
    }
}
