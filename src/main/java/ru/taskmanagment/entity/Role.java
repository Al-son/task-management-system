package ru.taskmanagment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "role_table")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = null;

    @Column(name = "authority", nullable = false)
    private String authority;
    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    @Column(name = "users")
    private List<User> users;

    public Role(String authority) {
        this.authority = authority;
    }

    public Role() {

    }

}

