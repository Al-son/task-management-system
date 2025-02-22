package ru.taskmanagment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.taskmanagment.entity.ConflictResolution;

public interface ConflictResolutionRepository extends JpaRepository<ConflictResolution, Long> {
}
