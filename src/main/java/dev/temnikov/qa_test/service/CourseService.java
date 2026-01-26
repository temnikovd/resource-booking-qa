package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.api.dto.RequestCourseDto;
import dev.temnikov.qa_test.api.dto.ResponseCourseDto;
import dev.temnikov.qa_test.api.mapper.CourseMapper;
import dev.temnikov.qa_test.entity.Course;
import dev.temnikov.qa_test.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public PageResponse<ResponseCourseDto> getAll(Pageable pageable) {
        Page<Course> page = courseRepository.findAll(pageable);

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(CourseMapper::toDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public ResponseCourseDto getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return CourseMapper.toDto(course);
    }

    public ResponseCourseDto create(RequestCourseDto dto) {
        Course course = CourseMapper.toEntity(dto);
        course.setId(null);
        Course saved = courseRepository.save(course);
        return CourseMapper.toDto(saved);
    }

    public ResponseCourseDto update(Long id, ResponseCourseDto dto) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        existing.setName(dto.name());

        Course saved = courseRepository.save(existing);
        return CourseMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        courseRepository.deleteById(id);
    }

    public Course getEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }
}
