package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.BookingDto;
import dev.temnikov.qa_test.entity.User;
import dev.temnikov.qa_test.service.BookingService;
import dev.temnikov.qa_test.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Bookings",
        description = """
                Booking API for creating, reading, updating and cancelling bookings.
                
                Key business rules (see RULES.md for full details):
                - Rule 6: Slot must start in the future to be bookable.
                - Rule 7: Slot must start in the future to allow cancellation.
                - Rule 8: Only the owning user or an ADMIN may create or cancel a booking.
                """
)
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @Operation(
            summary = "Get all bookings",
            description = """
                    Returns all bookings in the system.
                    
                    Requires authentication (USER or ADMIN).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of bookings returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public List<BookingDto> getAll() {
        return bookingService.getAll();
    }

    @Operation(
            summary = "Get booking by id",
            description = """
                    Returns a single booking by its identifier.
                    
                    Requires authentication (USER or ADMIN).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{id}")
    public BookingDto getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @Operation(
            summary = "Create a new booking",
            description = """
                    Creates a new booking for a slot.
                    
                    Business rules:
                    - Rule 6: Slot must start in the future.
                    - Rule 8: Only owning user or ADMIN may create a booking.
                      * If userId is omitted, the booking is created for the current user.
                      * If userId is provided, the caller must either be that user or have ADMIN role.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or slot is not in the future"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed to create a booking for the given user")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(
            @RequestBody BookingDto dto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {
        User currentUser = userService.getEntityByEmail(principal.getUsername());
        return bookingService.create(dto, currentUser);
    }

    @Operation(
            summary = "Update booking status",
            description = """
                    Updates the status of an existing booking.
                    
                    This endpoint does not currently enforce ownership or ADMIN checks and
                    is intended for exploring state transitions and error handling in tests.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PatchMapping("/{id}/status")
    public BookingDto updateStatus(@PathVariable Long id,
                                   @RequestParam("status") String status) {
        return bookingService.updateStatus(id, status);
    }

    @Operation(
            summary = "Cancel booking",
            description = """
                    Cancels an existing booking.
                    
                    Business rules:
                    - Rule 7: Slot must start in the future to allow cancellation.
                    - Rule 8: Only owning user or ADMIN may cancel a booking.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Slot is not in the future, cancellation forbidden"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Caller is not owner and not ADMIN"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PatchMapping("/{id}/cancel")
    public BookingDto cancel(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {
        User currentUser = userService.getEntityByEmail(principal.getUsername());
        return bookingService.cancel(id, currentUser);
    }

    @Operation(
            summary = "Delete booking",
            description = """
                    Deletes a booking by id.
                    
                    Intended mainly for cleanup in tests.
                    Requires authentication (USER or ADMIN).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }
}
