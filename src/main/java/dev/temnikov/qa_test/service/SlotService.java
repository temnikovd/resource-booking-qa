package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.SlotDto;
import dev.temnikov.qa_test.api.mapper.SlotMapper;
import dev.temnikov.qa_test.entity.Resource;
import dev.temnikov.qa_test.entity.Slot;
import dev.temnikov.qa_test.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final ResourceService resourceService;

    public List<SlotDto> getAll() {
        return slotRepository.findAll()
                .stream()
                .map(SlotMapper::toDto)
                .toList();
    }

    public SlotDto getById(Long id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        return SlotMapper.toDto(slot);
    }

    public SlotDto create(SlotDto dto) {
        if (dto.resourceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "resourceId is required");
        }
        if (dto.startTime() == null || dto.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime and endTime are required");
        }

        Resource resource = resourceService.getEntityById(dto.resourceId());

        LocalDateTime start = normalizeToMinutes(dto.startTime());
        LocalDateTime end = normalizeToMinutes(dto.endTime());

        validateSlotTimeRange(start, end);
        validateSlotInFuture(start);
        validateNoOverlap(resource.getId(), start, end, null);

        Slot slot = new Slot();
        slot.setResource(resource);
        slot.setStartTime(start);
        slot.setEndTime(end);

        Slot saved = slotRepository.save(slot);
        return SlotMapper.toDto(saved);
    }

    public SlotDto update(Long id, SlotDto dto) {
        Slot existing = slotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));

        Resource resource = existing.getResource();
        if (dto.resourceId() != null && !dto.resourceId().equals(resource.getId())) {
            resource = resourceService.getEntityById(dto.resourceId());
        }

        LocalDateTime start = dto.startTime() != null ? dto.startTime() : existing.getStartTime();
        LocalDateTime end = dto.endTime() != null ? dto.endTime() : existing.getEndTime();

        start = normalizeToMinutes(start);
        end = normalizeToMinutes(end);

        validateSlotTimeRange(start, end);
        validateSlotInFuture(start);
        validateNoOverlap(resource.getId(), start, end, existing.getId());

        existing.setResource(resource);
        existing.setStartTime(start);
        existing.setEndTime(end);

        Slot saved = slotRepository.save(existing);
        return SlotMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!slotRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found");
        }
        slotRepository.deleteById(id);
    }

    public Slot getEntityById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
    }

    private LocalDateTime normalizeToMinutes(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    private void validateSlotTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Slot endTime must be after startTime"
            );
        }
    }

    private void validateSlotInFuture(LocalDateTime start) {
        LocalDateTime now = LocalDateTime.now();
        if (!start.isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Slot startTime must be in the future"
            );
        }
    }

    private void validateNoOverlap(Long resourceId,
                                   LocalDateTime start,
                                   LocalDateTime end,
                                   Long currentSlotId) {

        List<Slot> overlapping = slotRepository.findOverlappingSlots(resourceId, start, end);

        boolean hasConflict = overlapping.stream()
                .anyMatch(slot -> currentSlotId == null || !slot.getId().equals(currentSlotId));

        if (hasConflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Slot overlaps with existing slot for this resource"
            );
        }
    }
}
