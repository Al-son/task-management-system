package ru.taskmanagment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.taskmanagment.entity.ConflictResolution;
import ru.taskmanagment.service.ConflictResolutionService;

import java.util.List;

@RestController
@RequestMapping("/conflict-resolutions")
@RequiredArgsConstructor
class ConflictResolutionController {

    private final ConflictResolutionService service;

    @GetMapping
    public List<ConflictResolution> getAllResolutions() {
        return service.getAllResolutions();
    }

    @GetMapping("/{id}")
    public ConflictResolution getResolutionById(@PathVariable Long id) {
        return service.getResolutionById(id);
    }
}
