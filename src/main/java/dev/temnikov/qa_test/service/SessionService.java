package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.api.dto.RequestSessionDto;
import dev.temnikov.qa_test.api.dto.ResponseSessionDto;
import dev.temnikov.qa_test.api.mapper.SessionMapper;
import dev.temnikov.qa_test.entity.BookingStatus;
import dev.temnikov.qa_test.entity.Course;
import dev.temnikov.qa_test.entity.Session;
import dev.temnikov.qa_test.repository.BookingRepository;
import dev.temnikov.qa_test.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final CourseService courseService;

    public PageResponse<ResponseSessionDto> getAll(Pageable pageable) {
        Page<Session> page = sessionRepository.findAll(pageable);

        Map<Long, Integer> currentBookingsBySessionId = loadCurrentBookings(page.getContent());

        return new PageResponse<>(
                page.getContent().stream()
                        .map(s -> SessionMapper.toDto(s, currentBookingsBySessionId.getOrDefault(s.getId(), 0)))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public ResponseSessionDto getById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        int currentBookings = (int) bookingRepository.countBySessionIdAndStatusIn(
                session.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        return SessionMapper.toDto(session, currentBookings);
    }

    private Map<Long, Integer> loadCurrentBookings(List<Session> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return Map.of();
        }

        Set<Long> sessionIds = sessions.stream()
                .map(Session::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (sessionIds.isEmpty()) {
            return Map.of();
        }

        List<BookingRepository.SessionBookingCount> counts =
                bookingRepository.countBySessionIdsAndStatusIn(
                        sessionIds,
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                );

        return counts.stream()
                .collect(Collectors.toMap(
                        BookingRepository.SessionBookingCount::getSessionId,
                        c -> (int) c.getCount()
                ));
    }

    public ResponseSessionDto create(RequestSessionDto dto) {
        if (dto.courseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseId is required");
        }
        if (dto.startTime() == null || dto.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime and endTime are required");
        }

        Course course = courseService.getEntityById(dto.courseId());

        LocalDateTime start = normalizeToMinutes(dto.startTime());
        LocalDateTime end = normalizeToMinutes(dto.endTime());

        validateSessionTimeRange(start, end);
        validateSessionInFuture(start);
        validateNoOverlap(course.getId(), start, end, null);
        int capacity = dto.capacity() != null ? dto.capacity() : Session.DEFAULT_CAPACITY;

        validateCapacity(capacity);


        Session session = new Session();
        session.setCourse(course);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setCapacity(capacity);

        Session saved = sessionRepository.save(session);
        return SessionMapper.toDto(saved);
    }

    public ResponseSessionDto update(Long id, RequestSessionDto dto) {
        Session existing = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        Course course = existing.getCourse();
        if (dto.courseId() != null && !dto.courseId().equals(course.getId())) {
            course = courseService.getEntityById(dto.courseId());
        }

        LocalDateTime start = dto.startTime() != null ? dto.startTime() : existing.getStartTime();
        LocalDateTime end = dto.endTime() != null ? dto.endTime() : existing.getEndTime();

        start = normalizeToMinutes(start);
        end = normalizeToMinutes(end);
        int capacity = dto.capacity() != null ? dto.capacity() : existing.getCapacity();


        validateSessionTimeRange(start, end);
        validateSessionInFuture(start);
        validateNoOverlap(course.getId(), start, end, existing.getId());
        validateCapacity(capacity);

        existing.setCapacity(capacity);
        existing.setCourse(course);
        existing.setStartTime(start);
        existing.setEndTime(end);

        Session saved = sessionRepository.save(existing);
        return SessionMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }
        sessionRepository.deleteById(id);
    }

    public Session getEntityById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    private LocalDateTime normalizeToMinutes(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    private void validateSessionTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session endTime must be after startTime"
            );
        }
    }

    private void validateSessionInFuture(LocalDateTime start) {
        LocalDateTime now = LocalDateTime.now();
        if (!start.isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session startTime must be in the future"
            );
        }
    }
    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session capacity must be greater than 0"
            );
        }
    }


    private void validateNoOverlap(Long courseId,
                                   LocalDateTime start,
                                   LocalDateTime end,
                                   Long currentSessionId) {

        List<Session> overlapping = sessionRepository.findOverlappingSessions(courseId, start, end);

        boolean hasConflict = overlapping.stream()
                .anyMatch(session -> currentSessionId == null || !session.getId().equals(currentSessionId));

        if (hasConflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Session overlaps with existing session for this course"
            );
        }
    }
}
