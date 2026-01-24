package dev.temnikov.qa_test.repository;

import dev.temnikov.qa_test.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Query("""
            select s
            from Slot s
            where s.resource.id = :resourceId
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    List<Slot> findOverlappingSlots(@Param("resourceId") Long resourceId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}