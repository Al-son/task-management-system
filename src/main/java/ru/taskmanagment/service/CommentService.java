package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.taskmanagment.entity.Comment;
import ru.taskmanagment.exception.CommentNotFoundException;
import ru.taskmanagment.payload.rq.CommentRq;
import ru.taskmanagment.payload.rs.CommentRs;
import ru.taskmanagment.repository.CommentRepository;
import ru.taskmanagment.repository.TaskRepository;
import ru.taskmanagment.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public CommentRs getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(String.format("Comment with id %d not found", id)));
        return CommentRs.toCommentRs(comment);
    }

    public CommentRs createComment(CommentRq commentRq) {
        Comment comment = commentRq.toComment(commentRq);
        Comment savedComment = commentRepository.save(comment);
        return CommentRs.toCommentRs(savedComment);
    }

    public String deleteComment(Long id) {
        Comment comment = getCommentById(id).toComment();
        commentRepository.delete(comment);
        return "Comment has been deleted";
    }

    public CommentRs updateComment(Long id, CommentRq commentRq) {
        Comment comment = getCommentById(id).toComment();
        comment.setContent(commentRq.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return CommentRs.toCommentRs(updatedComment);
    }
}
