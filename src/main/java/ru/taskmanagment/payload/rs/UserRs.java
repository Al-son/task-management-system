package ru.taskmanagment.payload.rs;

import lombok.Data;

@Data
public class UserRs {
    private Long id;
    private String name;
    private String email;

    public UserRs(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
