package com.example.exception;

public class InvalidInputParameterException extends RuntimeException {

    public InvalidInputParameterException(String message) {
        super(message);
    }
}
