package ru.taskmanagment.payload.rq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.lang.ref.PhantomReference;

@Data
public class GrantedRq {
    @NotNull
    private Long userId;
}
