package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.RequestCourseDto;
import dev.temnikov.qa_test.api.dto.ResponseCourseDto;
import dev.temnikov.qa_test.entity.Course;

public class CourseMapper {

    public static ResponseCourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }
        return new ResponseCourseDto(
                course.getId(),
                course.getName(),
                course.getTrainerId()
        );
    }

    public static Course toEntity(RequestCourseDto dto) {
        if (dto == null) {
            return null;
        }
        Course course = new Course();
        course.setName(dto.name());
        course.setTrainerId(dto.trainerId());
        return course;
    }
}
