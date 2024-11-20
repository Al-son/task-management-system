package ru.taskmanagment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.taskmanagment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
