package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.ClassDto;
import dev.temnikov.qa_test.entity.Course;

public class ClassMapper {

    public static ClassDto toDto(Course aCourse) {
        if (aCourse == null) {
            return null;
        }
        return new ClassDto(
                aCourse.getId(),
                aCourse.getName(),
                aCourse.getTrainerId()
        );
    }

    public static Course toEntity(ClassDto dto) {
        if (dto == null) {
            return null;
        }
        Course aCourse = new Course();
        aCourse.setId(dto.id());
        aCourse.setName(dto.name());
        aCourse.setTrainerId(dto.trainerId());
        return aCourse;
    }
}
