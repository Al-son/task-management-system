package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.taskmanagment.entity.ConflictResolution;
import ru.taskmanagment.repository.ConflictResolutionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConflictResolutionService {


    private final ConflictResolutionRepository repository;

    public List<ConflictResolution> getAllResolutions() {
        return repository.findAll();
    }

    public ConflictResolution getResolutionById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
