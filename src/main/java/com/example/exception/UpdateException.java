package com.example.exception;

public class UpdateException extends RuntimeException {

    public UpdateException(String errorMessage) {
        super(errorMessage);
    }
}