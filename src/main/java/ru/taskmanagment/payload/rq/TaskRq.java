package ru.taskmanagment.payload.rq;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.taskmanagment.entity.Comment;
import ru.taskmanagment.entity.Task;
import ru.taskmanagment.entity.Task.Status;
import ru.taskmanagment.entity.Task.Priority;

import java.util.List;

@Data
public class TaskRq {
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
    private Long id;
    private List<Comment> comments;

    public Task toTask(TaskRq taskRq) {
        Task task = new Task();
        task.setId(taskRq.getId());
        task.setTitle(taskRq.getTitle());
        task.setDescription(taskRq.getDescription());
        return task;
    }
}
