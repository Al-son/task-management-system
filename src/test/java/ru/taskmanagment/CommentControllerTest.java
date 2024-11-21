package ru.taskmanagment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.taskmanagment.controller.CommentController;
import ru.taskmanagment.payload.rq.CommentRq;
import ru.taskmanagment.payload.rs.CommentRs;
import ru.taskmanagment.service.CommentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentService commentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void getAllComments_ShouldReturnComments() throws Exception {
        // Prepare mock data
        CommentRs comment1 = new CommentRs(1L, "Fils");
        CommentRs comment2 = new CommentRs(1L, "Fils");
        List<CommentRs> comments = Arrays.asList(comment1, comment2);

        // Mock the service method
        //when(commentService.getAllComments()).thenReturn(comments);

        // Perform GET request
        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Test comment 1"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].text").value("Test comment 2"));
    }

    @Test
    void getComment_ShouldReturnSingleComment() throws Exception {
        // Prepare mock data
        CommentRs comment = new CommentRs(1L, "Fils");

        // Mock the service method
        when(commentService.getCommentById(anyLong())).thenReturn(comment);

        // Perform GET request
        mockMvc.perform(get("/comments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Fils"));
    }

    @Test
    void createComment_ShouldReturnCreatedComment() throws Exception {
        // Prepare mock data
        CommentRq commentRq = new CommentRq("New comment");
        CommentRs commentRs = new CommentRs(1L, "New comment");

        // Mock the service method
        when(commentService.createComment(any(CommentRq.class))).thenReturn(commentRs);

        // Perform POST request
        mockMvc.perform(post("/comments")
                        .contentType("application/json")
                        .content("{\"text\":\"New comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("New comment"));
    }

    @Test
    void updateComment_ShouldReturnUpdatedComment() throws Exception {
        // Prepare mock data
        CommentRq commentRq = new CommentRq("Updated comment");
        CommentRs updatedCommentRs = new CommentRs(1L, "Updated comment");

        // Mock the service method
        when(commentService.updateComment(anyLong(), any(CommentRq.class))).thenReturn(updatedCommentRs);

        // Perform PUT request
        mockMvc.perform(put("/comments/{id}", 1)
                        .contentType("application/json")
                        .content("{\"text\":\"Updated comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    @Test
    void deleteComment_ShouldReturnSuccessMessage() throws Exception {
        // Mock the service method
        when(commentService.deleteComment(anyLong())).thenReturn("Comment deleted");

        // Perform DELETE request
        mockMvc.perform(delete("/comments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted"));
    }
}
