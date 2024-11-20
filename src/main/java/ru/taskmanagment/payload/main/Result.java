package ru.taskmanagment.payload.main;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Result<T> {
    private String message;
    private T data;
}
