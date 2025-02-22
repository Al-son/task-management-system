package ru.taskmanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "conflict_resolutions")
@Getter
@Setter
@Data
public class ConflictResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;
    @Column(name = "strategy", columnDefinition = "TEXT")
    private String strategy;
    @Column(name = "diff_before", columnDefinition = "TEXT")
    private String diffBefore;
    @Column(name = "diff_after", columnDefinition = "TEXT")
    private String diffAfter;
    @Column(name = "resolved_at", columnDefinition = "TEXT")
    private LocalDateTime resolvedAt;

    public ConflictResolution() {
        this.resolvedAt = LocalDateTime.now();
    }

    public ConflictResolution(String filePath,
                              String strategy,
                              String diffBefore,
                              String diffAfter) {
        this.filePath = filePath;
        this.strategy = strategy;
        this.diffBefore = diffBefore;
        this.diffAfter = diffAfter;
        this.resolvedAt = LocalDateTime.now();
    }
}
