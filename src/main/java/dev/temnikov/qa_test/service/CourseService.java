package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.api.dto.ClassDto;
import dev.temnikov.qa_test.api.mapper.ClassMapper;
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

    public PageResponse<ClassDto> getAll(Pageable pageable) {
        Page<Course> page = courseRepository.findAll(pageable);

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(ClassMapper::toDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public ClassDto getById(Long id) {
        Course aCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        return ClassMapper.toDto(aCourse);
    }

    public ClassDto create(ClassDto dto) {
        Course aCourse = ClassMapper.toEntity(dto);
        aCourse.setId(null);
        Course saved = courseRepository.save(aCourse);
        return ClassMapper.toDto(saved);
    }

    public ClassDto update(Long id, ClassDto dto) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        existing.setName(dto.name());

        Course saved = courseRepository.save(existing);
        return ClassMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        courseRepository.deleteById(id);
    }

    public Course getEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
    }
}
