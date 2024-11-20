package ru.taskmanagment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.taskmanagment.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
