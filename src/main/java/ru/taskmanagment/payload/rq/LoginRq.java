package ru.taskmanagment.payload.rq;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@RequiredArgsConstructor
public class LoginRq {
    @NotBlank
    @Length(min = 3, max = 50)
    private String email;
    @NotBlank
    @Length(min = 4)
    private String password;
}
