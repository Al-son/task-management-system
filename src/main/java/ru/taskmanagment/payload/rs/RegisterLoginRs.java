package ru.taskmanagment.payload.rs;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterLoginRs {
    private String access_token;

    public RegisterLoginRs(String access_token) {
        this.access_token = access_token;
    }
}
