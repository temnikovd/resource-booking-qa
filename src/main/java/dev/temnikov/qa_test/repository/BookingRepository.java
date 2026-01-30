package dev.temnikov.qa_test.repository;

import dev.temnikov.qa_test.entity.Booking;
import dev.temnikov.qa_test.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countBySessionIdAndStatusIn(Long sessionId, Collection<BookingStatus> statuses);

    interface SessionBookingCount {
        Long getSessionId();
        long getCount();
    }

    @Query("select b.session.id as sessionId, count(b) as count " +
            "from Booking b " +
            "where b.session.id in :sessionIds and b.status in :statuses " +
            "group by b.session.id")
    List<SessionBookingCount> countBySessionIdsAndStatusIn(
            @Param("sessionIds") Collection<Long> sessionIds,
            @Param("statuses") Collection<BookingStatus> statuses
    );

}
