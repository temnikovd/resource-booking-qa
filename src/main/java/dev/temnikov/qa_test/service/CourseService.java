package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.PageResponse;
import dev.temnikov.qa_test.api.dto.CourseDto;
import dev.temnikov.qa_test.api.mapper.ClassMapper;
import dev.temnikov.qa_test.entity.Course;
import dev.temnikov.qa_test.entity.User;
import dev.temnikov.qa_test.repository.CourseRepository;
import dev.temnikov.qa_test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static dev.temnikov.qa_test.entity.UserRole.TRAINER;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;

    public PageResponse<CourseDto> getAll(Pageable pageable) {
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

    public CourseDto getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return ClassMapper.toDto(course);
    }

    public CourseDto create(CourseDto dto, User currentUser) {
        User trainer = null;
        if (currentUser != null && TRAINER.equals(currentUser.getRole())) {
            trainer = currentUser;
        } else {
            if (dto.trainerId() == null)  {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Correct trainer ID should be provided");
            }
            trainer = userService.getOptEntityById(dto.trainerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Correct trainer ID should be provided"));
        }
        Course course = ClassMapper.toEntity(dto);
        course.setTrainerId(trainer.getId());
        Course saved = courseRepository.save(course);
        return ClassMapper.toDto(saved);
    }

    public CourseDto update(Long id, CourseDto dto) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        existing.setName(dto.name());

        Course saved = courseRepository.save(existing);
        return ClassMapper.toDto(saved);
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
