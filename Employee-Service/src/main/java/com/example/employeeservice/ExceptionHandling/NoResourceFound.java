package com.example.employeeservice.ExceptionHandling;

public class NoResourceFound extends RuntimeException {
    public NoResourceFound(String message) {
        super(message);
    }
}
