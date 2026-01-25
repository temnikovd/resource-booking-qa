package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.CourseDto;
import dev.temnikov.qa_test.entity.Course;

public class ClassMapper {

    public static CourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseDto(
                course.getId(),
                course.getName(),
                course.getTrainerId()
        );
    }

    public static Course toEntity(CourseDto dto) {
        if (dto == null) {
            return null;
        }
        Course course = new Course();
        course.setId(dto.id());
        course.setName(dto.name());
        course.setTrainerId(dto.trainerId());
        return course;
    }
}
