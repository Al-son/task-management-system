package ru.taskmanagment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.taskmanagment.entity.Task;
import ru.taskmanagment.payload.rq.TaskRq;
import ru.taskmanagment.payload.rs.TaskRs;
import ru.taskmanagment.repository.TaskRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations for this test class
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRq taskRq;
    private TaskRs taskRs;


    @Test
    void getAllTasks() {
        Task task = new Task();
        task.setTitle("Test Task");
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task));

        var tasks = taskService.getAllTasks();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void getTaskById() {
        Task task = new Task();
        task.setTitle("Test Task");
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));

        TaskRs result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(task.getTitle(), result.getTitle());
    }
}
