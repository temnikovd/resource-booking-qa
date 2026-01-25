package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.BookingDto;
import dev.temnikov.qa_test.entity.Booking;
import dev.temnikov.qa_test.entity.BookingStatus;
import dev.temnikov.qa_test.entity.Session;
import dev.temnikov.qa_test.entity.User;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        Long sessionId = booking.getSession() != null ? booking.getSession().getId() : null;

        return new BookingDto(
                booking.getId(),
                userId,
                sessionId,
                booking.getStatus() != null ? booking.getStatus().name() : null
        );
    }


    public static Booking toEntity(BookingDto dto, User user, Session session) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setId(dto.id());
        booking.setUser(user);
        booking.setSession(session);
        if (dto.status() != null) {
            booking.setStatus(BookingStatus.valueOf(dto.status()));
        }
        return booking;
    }
}
