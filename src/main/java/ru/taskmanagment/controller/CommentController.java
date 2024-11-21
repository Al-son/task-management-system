package ru.taskmanagment.controller;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.taskmanagment.payload.rq.CommentRq;
import ru.taskmanagment.payload.rs.CommentRs;
import ru.taskmanagment.service.CommentService;

import java.util.List;

import static ru.taskmanagment.util.RoleLocal.ADMIN;
import static ru.taskmanagment.util.RoleLocal.USER;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    //@RolesAllowed({USER, ADMIN})
    public ResponseEntity<List<CommentRs>> getAllComments() {
        List<CommentRs> comments = commentService.getAllComments()
                .stream()
                .map(CommentRs::toCommentRs)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    //@RolesAllowed(ADMIN)
    public ResponseEntity<CommentRs> getComment(@PathVariable Long id) {
        CommentRs commentRs = commentService.getCommentById(id);
        return ResponseEntity.ok(commentRs);
    }

    @PostMapping
    @RolesAllowed({USER, ADMIN})
    public ResponseEntity<CommentRs> createComment(@RequestBody CommentRq commentRq) {
        CommentRs commentRs = commentService.createComment(commentRq);
        return ResponseEntity.ok(commentRs);
    }

    @PutMapping("/{id}")
    @RolesAllowed({USER, ADMIN})
    public ResponseEntity<CommentRs> updateComment(@PathVariable Long id, @RequestBody CommentRq commentRq) {
        CommentRs updatedComment = commentService.updateComment(id, commentRq);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(ADMIN)
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        String response = commentService.deleteComment(id);
        return ResponseEntity.ok(response);
    }
}
