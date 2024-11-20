package ru.taskmanagment.exception;

public class CustomerRoleNotFoundException extends RuntimeException {
    public CustomerRoleNotFoundException(String message) {
        super(message);
    }
}
