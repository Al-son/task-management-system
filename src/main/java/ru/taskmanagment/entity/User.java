package ru.taskmanagment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;
import ru.taskmanagment.payload.rs.UserAuth;
import ru.taskmanagment.payload.rs.UserRs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "user_table")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "password")
    @JsonIgnore
    private String password;
    @Column(name = "timeCreationToken")
    @JsonIgnore
    @CreationTimestamp
    private LocalDateTime timeCreationToken;
    @Column(name = "resetToken")
    private String resetToken;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Task> tasks =  new ArrayList<>();
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public User(@NotBlank @Length(min = 3, max = 50) String name, @NotBlank @Email @Length(min = 5, max = 50) String email, @NotBlank @Length(min = 5) String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UserAuth toUserAuth() {
        return new UserAuth(
                this.email,
                this.password
        );
    }

    public UserRs toUserRs() {
        return new UserRs(
                this.id,
                this.name,
                this.email,
                this.roles
        );
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}
