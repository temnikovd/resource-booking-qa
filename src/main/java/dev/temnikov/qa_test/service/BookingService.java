package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.RequestBookingDto;
import dev.temnikov.qa_test.api.dto.ResponseBookingDto;
import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.api.mapper.BookingMapper;
import dev.temnikov.qa_test.entity.*;
import dev.temnikov.qa_test.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SessionService sessionService;
    private final UserService userService;


    public PageResponse<ResponseBookingDto> getAll(Pageable pageable) {
        Page<Booking> page = bookingRepository.findAll(pageable);

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(BookingMapper::toResponseDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }


    public ResponseBookingDto getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        return BookingMapper.toResponseDto(booking);
    }

    /**
     * Only owner (dto.userId) or ADMIN can create booking.
     * Session must be in the future.
     */
    public ResponseBookingDto create(RequestBookingDto dto, User currentUser) {
        if (dto.sessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required");
        }
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user is required");
        }

        Long targetUserId = dto.userId() != null ? dto.userId() : currentUser.getId();

        if (!isOwnerOrAdmin(targetUserId, currentUser)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owning user or admin may create booking"
            );
        }

        User user = userService.getEntityById(targetUserId);
        Session session = sessionService.getEntityById(dto.sessionId());

        LocalDateTime now = LocalDateTime.now();
        if (!session.getStartTime().isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session must be in the future to create booking"
            );
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSession(session);
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(saved);
    }

    /**
     * Only owner or ADMIN may cancel.
     * Session must be in the future to cancel.
     */
    public ResponseBookingDto cancel(Long id, User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user is required");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        Session session = booking.getSession();
        LocalDateTime now = LocalDateTime.now();

        if (!session.getStartTime().isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session must be in the future to cancel booking"
            );
        }

        if (!isOwnerOrAdmin(booking.getUser().getId(), currentUser)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only owning user or admin may cancel booking"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(saved);
    }

    /**
     * Simple status update (no owner/admin rules here unless you want to add them later).
     */
    public ResponseBookingDto updateStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }

        booking.setStatus(newStatus);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(saved);
    }

    private boolean isOwnerOrAdmin(Long ownerId, User currentUser) {
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRole() == UserRole.ADMIN) {
            return true;
        }
        return ownerId.equals(currentUser.getId());
    }

    public void delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        bookingRepository.deleteById(id);
    }
}
