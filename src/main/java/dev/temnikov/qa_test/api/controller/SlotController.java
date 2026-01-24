package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.SlotDto;
import dev.temnikov.qa_test.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(
        name = "Slots",
        description = """
                Time slots for resources.
                
                Reads require authentication (USER or ADMIN).
                Writes are restricted to ADMIN users.
                
                Business rules (see RULES.md):
                - Slots must be in the future.
                - Slots must not overlap for the same resource.
                - Slots belong to an existing resource.
                """
)
public class SlotController {

    private final SlotService slotService;

    @Operation(
            summary = "Get all slots",
            description = "Returns all slots. Requires authentication (USER or ADMIN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slots returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public List<SlotDto> getAll() {
        return slotService.getAll();
    }

    @Operation(
            summary = "Get slot by id",
            description = "Returns a single slot by id. Requires authentication (USER or ADMIN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @GetMapping("/{id}")
    public SlotDto getById(@PathVariable Long id) {
        return slotService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new slot (ADMIN only)",
            description = """
                    Creates a new slot for a given resource.
                    
                    Requires authentication and ADMIN role.
                    Business rules:
                    - Slots must start in the future.
                    - Slots must not overlap with existing slots for the same resource.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Slot created"),
            @ApiResponse(responseCode = "400", description = "Validation error (past time or overlapping slot)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User lacks ADMIN role")
    })
    public SlotDto create(@RequestBody SlotDto dto) {
        return slotService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing slot (ADMIN only)",
            description = """
                    Updates an existing slot.
                    
                    Requires authentication and ADMIN role.
                    Same business rules apply as for creation (future-only, non-overlapping).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot updated"),
            @ApiResponse(responseCode = "400", description = "Validation error (past time or overlapping slot)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User lacks ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    public SlotDto update(@PathVariable Long id, @RequestBody SlotDto dto) {
        return slotService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a slot (ADMIN only)",
            description = """
                    Deletes a slot by id.
                    
                    Requires authentication and ADMIN role.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Slot deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User lacks ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    public void delete(@PathVariable Long id) {
        slotService.delete(id);
    }
}
