package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.api.dto.SlotDto;
import dev.temnikov.qa_test.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping
    public List<SlotDto> getAll() {
        return slotService.getAll();
    }

    @GetMapping("/{id}")
    public SlotDto getById(@PathVariable Long id) {
        return slotService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotDto create(@RequestBody SlotDto dto) {
        return slotService.create(dto);
    }

    @PutMapping("/{id}")
    public SlotDto update(@PathVariable Long id, @RequestBody SlotDto dto) {
        return slotService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        slotService.delete(id);
    }
}
