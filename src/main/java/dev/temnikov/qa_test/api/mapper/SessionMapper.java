package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.RequestSessionDto;
import dev.temnikov.qa_test.api.dto.ResponseSessionDto;
import dev.temnikov.qa_test.entity.Course;
import dev.temnikov.qa_test.entity.Session;

public class SessionMapper {

    public static ResponseSessionDto toDto(Session session) {
        if (session == null) {
            return null;
        }
        Long courseId = session.getCourse() != null ? session.getCourse().getId() : null;
        return new ResponseSessionDto(
                session.getId(),
                courseId,
                session.getStartTime(),
                session.getEndTime()
        );
    }


    public static Session toEntity(RequestSessionDto dto, Course course) {
        if (dto == null) {
            return null;
        }
        Session session = new Session();
        session.setCourse(course);
        session.setStartTime(dto.startTime());
        session.setEndTime(dto.endTime());
        return session;
    }
}
