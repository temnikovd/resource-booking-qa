package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.RequestBookingDto;
import dev.temnikov.qa_test.api.dto.ResponseBookingDto;
import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.entity.User;
import dev.temnikov.qa_test.security.SecurityUser;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "Bookings",
        description = """
                API for booking scheduled sessions of a course.
                
                Business rules (short form):
                - Sessions must start in the future to be bookable.
                - Sessions must start in the future to be cancellable.
                - Only the owning user or an ADMIN may create or cancel a booking.
                
                See RULES.md for the full list of constraints enforced by the service layer.
                """
)
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @Operation(
            summary = "List bookings (paginated)",
            description = """
                    Returns paginated bookings for administrative and testing scenarios.
                    
                    Query parameters:
                    - page: zero-based page index (default 0)
                    - size: page size (default 20)
                    - sort: field and direction (e.g. sort=id,asc or sort=startTime,desc)
                    
                    Requires authentication (USER / TRAINER / ADMIN).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public PageResponse<ResponseBookingDto> getAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return bookingService.getAll(pageable);
    }

    @Operation(
            summary = "Get booking by ID",
            description = """
                    Returns a specific booking.
                    
                    Requires authentication.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{id}")
    public ResponseBookingDto getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @Operation(
            summary = "Create a booking",
            description = """
                    Books a session for a user.
                    
                    Business rules:
                    - Session must start in the future.
                    - Caller must either be the booking owner or have ADMIN role.
                      * If userId is omitted in the payload, the current user is assumed.
                      * If userId is provided, the caller must match or be ADMIN.
                    
                    Requires authentication.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created",
                    content = @Content(schema = @Schema(implementation = ResponseBookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or session is not in the future"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Not allowed to create booking for the specified user"),
            @ApiResponse(responseCode = "409", description = "Session capacity reached")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseBookingDto create(
            @RequestBody RequestBookingDto dto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal SecurityUser principal
    ) {
        User currentUser = userService.getEntityByEmail(principal.getUsername());
        return bookingService.create(dto, currentUser);
    }

    @Operation(
            summary = "Update booking status",
            description = """
                    Modifies the status of a booking.
                    
                    This endpoint is intentionally permissive to support testing of
                    state transitions and invalid scenarios.
                    
                    Requires authentication.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = ResponseBookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseBookingDto updateStatus(@PathVariable Long id,
                                           @RequestParam("status") String status) {
        return bookingService.updateStatus(id, status);
    }

    @Operation(
            summary = "Cancel a booking",
            description = """
                    Cancels a booking.
                    
                    Business rules:
                    - Session must start in the future.
                    - Only owning user or ADMIN may cancel.
                    
                    Requires authentication.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled",
                    content = @Content(schema = @Schema(implementation = ResponseBookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Session is not in the future"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Not allowed to cancel booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseBookingDto cancel(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal SecurityUser principal
    ) {
        User currentUser = userService.getEntityByEmail(principal.getUsername());
        return bookingService.cancel(id, currentUser);
    }

    @Operation(
            summary = "Delete booking",
            description = """
                    Deletes a booking by ID.
                    
                    Requires authentication.
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
