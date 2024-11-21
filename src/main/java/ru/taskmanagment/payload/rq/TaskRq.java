package ru.taskmanagment.payload.rq;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.taskmanagment.entity.Task;
import ru.taskmanagment.enumClass.Priority;
import ru.taskmanagment.enumClass.Status;


@Data
public class TaskRq {
    private Long userId;
    @NotNull(message = "Title cannot be null")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;
    @NotNull(message = "Description cannot be null")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;
    @NotNull(message = "Status cannot be null")
    private Status status;
    @NotNull(message = "Priority cannot be null")
    private Priority priority;



    public Task toTask(TaskRq taskRq) {
        Task task = new Task();
        task.setUserId(taskRq.getUserId());
        task.setPriority(taskRq.getPriority());
        task.setStatus(taskRq.getStatus());
        task.setTitle(taskRq.getTitle());
        task.setDescription(taskRq.getDescription());
        return task;
    }
}
