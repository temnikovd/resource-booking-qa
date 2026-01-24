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
@Tag(name = "Resources", description = "Bookable resources (ADMIN only for write)")
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(
            summary = "Get all resources",
            description = "Requires authentication (USER or ADMIN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public List<ResourceDto> getAll() {
        return resourceService.getAll();
    }

    @GetMapping("/{id}")
    public ResourceDto getById(@PathVariable Long id) {
        return resourceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new resource (ADMIN only)",
            description = """
                Requires authentication and ADMIN role.
                
                Business rule:
                - Rule 3: Only ADMIN can create/update/delete resources
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
    public ResourceDto update(@PathVariable Long id, @RequestBody ResourceDto dto) {
        return resourceService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        resourceService.delete(id);
    }
}
