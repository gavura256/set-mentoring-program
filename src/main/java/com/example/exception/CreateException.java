package com.example.exception;

public class CreateException extends RuntimeException {

    public CreateException(String errorMessage) {
        super(errorMessage);
    }
}