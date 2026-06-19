package com.example.addressservice.ExceptionHandling;

public class NoResourceFound extends RuntimeException {
    public NoResourceFound(String message) {
        super(message);
    }
}
