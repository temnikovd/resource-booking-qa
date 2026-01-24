package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.BookingDto;
import dev.temnikov.qa_test.entity.Booking;
import dev.temnikov.qa_test.entity.BookingStatus;
import dev.temnikov.qa_test.entity.Slot;
import dev.temnikov.qa_test.entity.User;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        Long slotId = booking.getSlot() != null ? booking.getSlot().getId() : null;

        return new BookingDto(
                booking.getId(),
                userId,
                slotId,
                booking.getStatus() != null ? booking.getStatus().name() : null
        );
    }


    public static Booking toEntity(BookingDto dto, User user, Slot slot) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setId(dto.id());
        booking.setUser(user);
        booking.setSlot(slot);
        if (dto.status() != null) {
            booking.setStatus(BookingStatus.valueOf(dto.status()));
        }
        return booking;
    }
}
