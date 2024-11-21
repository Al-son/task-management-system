package ru.taskmanagment.payload.rq;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class GrantedRq {
    @NotNull
    private Long userId;
}
