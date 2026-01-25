package dev.temnikov.qa_test.repository;

import dev.temnikov.qa_test.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
            select s
            from Session s
            where s.course.id = :courseId
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    List<Session> findOverlappingSessions(@Param("courseId") Long courseId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
}