package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.SlotDto;
import dev.temnikov.qa_test.entity.Resource;
import dev.temnikov.qa_test.entity.Slot;

public class SlotMapper {

    public static SlotDto toDto(Slot slot) {
        if (slot == null) {
            return null;
        }
        Long resourceId = slot.getResource() != null ? slot.getResource().getId() : null;
        return new SlotDto(
                slot.getId(),
                resourceId,
                slot.getStartTime(),
                slot.getEndTime()
        );
    }


    public static Slot toEntity(SlotDto dto, Resource resource) {
        if (dto == null) {
            return null;
        }
        Slot slot = new Slot();
        slot.setResource(resource);
        slot.setStartTime(dto.startTime());
        slot.setEndTime(dto.endTime());
        return slot;
    }
}
