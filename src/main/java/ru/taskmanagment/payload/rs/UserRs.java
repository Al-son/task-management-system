package ru.taskmanagment.payload.rs;

import lombok.Data;
import ru.taskmanagment.entity.Role;

import java.util.Set;

@Data
public class UserRs {
    private Long id;
    private String name;
    private String email;
    private Set<Role> roles;

    public UserRs(Long id, String name, String email, Set<Role> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }
}
