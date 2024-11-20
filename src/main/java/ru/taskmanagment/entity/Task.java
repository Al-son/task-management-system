package ru.taskmanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="user_id")
    private Long userId;
    @Column(name = "title", nullable = false, unique = true)
    private String title;
    @Column(name = "description", nullable = false, unique = true)
    private String description;
    @Column(name = "status", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "priority", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false, insertable = false)
    private User user;
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    public enum Priority {
        HIGH, MEDIUM, LOW
    }
}
