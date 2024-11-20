package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.taskmanagment.entity.Task;
import ru.taskmanagment.exception.TaskNotFoundException;
import ru.taskmanagment.payload.rq.TaskRq;
import ru.taskmanagment.payload.rs.TaskRs;
import ru.taskmanagment.repository.TaskRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public TaskRs getTaskById(Long id) {
        Task tasks = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(String.format("Task with %id not found", id)));
        return TaskRs.toTaskRs(tasks);
    }

    public TaskRs createTask(TaskRq taskRq) {
        Task task = taskRq.toTask(taskRq);
        Task savedTask = taskRepository.save(task);
        return TaskRs.toTaskRs(savedTask);
    }

    public String deleteTask(Long id) {
        Task task = getTaskById(id).toTask();
        taskRepository.delete(task);
        return "Task has been deleted";
    }

    public TaskRs updateTask(Long id, TaskRq taskRq) {
        Task task = getTaskById(id).toTask();
        task.setId(task.getId());
        task.setTitle(task.getTitle());
        task.setDescription(taskRq.getDescription());
        task.setStatus(taskRq.getStatus());
        task.setPriority(taskRq.getPriority());
        task.setComments(taskRq.getComments());
        Task updatedTask = taskRepository.save(task);
        return TaskRs.toTaskRs(updatedTask);
    }
}
