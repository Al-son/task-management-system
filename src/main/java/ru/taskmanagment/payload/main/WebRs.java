package ru.taskmanagment.payload.main;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.taskmanagment.payload.rs.RegisterLoginRs;
import ru.taskmanagment.payload.rs.UserRs;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebRs<T> {
    private int code;
    private String message;
    private T data;

    public WebRs(RegisterLoginRs registerLoginRs) {
        this.code = 200;
        this.message = "Success";
        this.data = (T) registerLoginRs;
    }

    public WebRs(UserRs userRs) {
        this.code = 200;
        this.message = "Success";
        this.data = (T) userRs;
    }

    public WebRs(String s) {
        this.code = 200;
        this.message = "Success";
        this.data = (T) s;
    }

    public T getName() {
        return this.data;
    }
}
