package dev.temnikov.qa_test.repository;

import dev.temnikov.qa_test.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
