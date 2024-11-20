package ru.taskmanagment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.taskmanagment.payload.rq.CommentRq;
import ru.taskmanagment.payload.rs.CommentRs;
import ru.taskmanagment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentRs>> getAllComments() {
        List<CommentRs> comments = commentService.getAllComments()
                .stream()
                .map(CommentRs::toCommentRs)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentRs> getComment(@PathVariable Long id) {
        CommentRs commentRs = commentService.getCommentById(id);
        return ResponseEntity.ok(commentRs);
    }

    @PostMapping
    public ResponseEntity<CommentRs> createComment(@RequestBody CommentRq commentRq) {
        CommentRs commentRs = commentService.createComment(commentRq);
        return ResponseEntity.ok(commentRs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentRs> updateComment(@PathVariable Long id, @RequestBody CommentRq commentRq) {
        CommentRs updatedComment = commentService.updateComment(id, commentRq);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        String response = commentService.deleteComment(id);
        return ResponseEntity.ok(response);
    }
}
