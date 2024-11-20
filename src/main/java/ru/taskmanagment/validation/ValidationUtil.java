package ru.taskmanagment.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ValidationUtil {
    private final Validator validator;

    public ValidationUtil(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> violationSet = validator.validate(object);
        if (!violationSet.isEmpty()) {
            throw new ConstraintViolationException(violationSet);
        }
    }
}
