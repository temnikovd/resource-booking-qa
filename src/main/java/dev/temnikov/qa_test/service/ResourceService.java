package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.ResourceDto;
import dev.temnikov.qa_test.api.mapper.ResourceMapper;
import dev.temnikov.qa_test.entity.Resource;
import dev.temnikov.qa_test.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public List<ResourceDto> getAll() {
        return resourceRepository.findAll()
                .stream()
                .map(ResourceMapper::toDto)
                .toList();
    }

    public ResourceDto getById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        return ResourceMapper.toDto(resource);
    }

    public ResourceDto create(ResourceDto dto) {
        Resource resource = ResourceMapper.toEntity(dto);
        resource.setId(null);
        Resource saved = resourceRepository.save(resource);
        return ResourceMapper.toDto(saved);
    }

    public ResourceDto update(Long id, ResourceDto dto) {
        Resource existing = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        existing.setName(dto.name());

        Resource saved = resourceRepository.save(existing);
        return ResourceMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        resourceRepository.deleteById(id);
    }

    public Resource getEntityById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
    }
}
