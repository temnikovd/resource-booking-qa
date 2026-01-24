package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.ResourceDto;
import dev.temnikov.qa_test.entity.Resource;

public class ResourceMapper {

    public static ResourceDto toDto(Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ResourceDto(
                resource.getId(),
                resource.getName()
        );
    }

    public static Resource toEntity(ResourceDto dto) {
        if (dto == null) {
            return null;
        }
        Resource resource = new Resource();
        resource.setId(dto.id());
        resource.setName(dto.name());
        return resource;
    }
}
