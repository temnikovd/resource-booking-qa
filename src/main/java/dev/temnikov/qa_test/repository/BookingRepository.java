package dev.temnikov.qa_test.repository;

import dev.temnikov.qa_test.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
