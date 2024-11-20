package ru.taskmanagment.payload.rq;

import lombok.Data;
import ru.taskmanagment.entity.Comment;

@Data
public class CommentRq {
    private Long userId;
    private Long taskId;
    private String content;

    public CommentRq(Long userId, Long taskId, String content) {
        this.userId = userId;
        this.taskId = taskId;
        this.content = content;
    }

    public Comment toComment(CommentRq commentRq) {
        Comment comment = new Comment();
        comment.setUserId(commentRq.getUserId());
        comment.setTaskId(commentRq.getTaskId());
        comment.setContent(commentRq.getContent());
        return comment;
    }
}
