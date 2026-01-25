package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.api.dto.SessionDto;
import dev.temnikov.qa_test.api.mapper.SessionMapper;
import dev.temnikov.qa_test.entity.Course;
import dev.temnikov.qa_test.entity.Session;
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

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CourseService courseService;

    public PageResponse<SessionDto> getAll(Pageable pageable) {
        Page<Session> page = sessionRepository.findAll(pageable);

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(SessionMapper::toDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public SessionDto getById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        return SessionMapper.toDto(session);
    }

    public SessionDto create(SessionDto dto) {
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

        Session session = new Session();
        session.setCourse(course);
        session.setStartTime(start);
        session.setEndTime(end);

        Session saved = sessionRepository.save(session);
        return SessionMapper.toDto(saved);
    }

    public SessionDto update(Long id, SessionDto dto) {
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

        validateSessionTimeRange(start, end);
        validateSessionInFuture(start);
        validateNoOverlap(course.getId(), start, end, existing.getId());

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
