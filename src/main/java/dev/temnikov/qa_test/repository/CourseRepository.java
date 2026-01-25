package dev.temnikov.qa_test.repository;

import dev.temnikov.qa_test.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
