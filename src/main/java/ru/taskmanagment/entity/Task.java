package ru.taskmanagment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", updatable = false, insertable = false)
    private User user;
    @JsonIgnore
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    public enum Priority {
        HIGH, MEDIUM, LOW
    }
}
