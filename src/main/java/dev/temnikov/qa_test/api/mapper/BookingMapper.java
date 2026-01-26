package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.RequestBookingDto;
import dev.temnikov.qa_test.api.dto.ResponseBookingDto;
import dev.temnikov.qa_test.entity.Booking;
import dev.temnikov.qa_test.entity.BookingStatus;
import dev.temnikov.qa_test.entity.Session;
import dev.temnikov.qa_test.entity.User;

public class BookingMapper {

    public static ResponseBookingDto toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        Long sessionId = booking.getSession() != null ? booking.getSession().getId() : null;

        return new ResponseBookingDto(
                booking.getId(),
                userId,
                sessionId,
                booking.getStatus() != null ? booking.getStatus().name() : null
        );
    }


    public static Booking toEntity(RequestBookingDto dto, User user, Session session) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSession(session);
        if (dto.status() != null) {
            booking.setStatus(BookingStatus.valueOf(dto.status()));
        }
        return booking;
    }
}
