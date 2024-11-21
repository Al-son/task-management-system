package ru.taskmanagment.payload.rs;

import lombok.Data;
import lombok.Generated;
import ru.taskmanagment.entity.Comment;
import ru.taskmanagment.entity.Task;
import ru.taskmanagment.entity.User;

import java.util.List;

@Data
public class TaskRs {

    private Long id;
    private String title;
    private Long userId;
    private String description;
    private Task.Status status;
    private Task.Priority priority;
    private List<Comment> comments;

    public static TaskRs toTaskRs(Task task) {
        TaskRs taskRs = new TaskRs();
        taskRs.setId(task.getId());
        taskRs.setTitle(task.getTitle());
        taskRs.setUserId(task.getUserId());
        taskRs.setComments(task.getComments());
        taskRs.setPriority(task.getPriority());
        taskRs.setDescription(task.getDescription());
        taskRs.setStatus(task.getStatus());
        return taskRs;
    }

    public Task toTask() {
        Task task = new Task();
        task.setId(getId());
        task.setTitle(getTitle());
        task.setPriority(getPriority());
        task.setStatus(getStatus());
        task.setComments(getComments());
        task.setDescription(getDescription());
        return task;
    }

}
