package ru.taskmanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "role_table")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = null;

    @Column(name = "authority", unique = true, nullable = false)
    private String authority;

    @ManyToMany(mappedBy = "roles")
    @Column(name = "users")
    private List<User> users;

//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;

    public Role(String authority) {
        this.authority = authority;
    }

    public Role() {

    }

}

