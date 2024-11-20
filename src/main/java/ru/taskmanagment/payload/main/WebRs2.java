package ru.taskmanagment.payload.main;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@RequiredArgsConstructor
public class WebRs2<T> {
    private HttpStatus code;
    private Result<T> result;
}
