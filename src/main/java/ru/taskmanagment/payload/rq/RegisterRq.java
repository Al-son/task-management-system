package ru.taskmanagment.payload.rq;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.taskmanagment.entity.User;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RegisterRq {
    @NotBlank
    @Length(min = 3, max = 50)
    private String name;
    @NotBlank
    @Email
    @Length(min = 4, max = 50)
    private String email;
    @NotBlank
    @Length(min = 4)
    private String password;

    public RegisterRq(String encode) {
        this.password = encode;
    }

    public User toUsers() {
        return new User(
                this.name,
                this.email,
                this.password
        );
    }
}