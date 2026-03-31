package com.example.dto.wrapper;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public class ExceptionResponse {

    private HttpStatus httpStatus;

    private String details;

    public ExceptionResponse(HttpStatus httpStatus, String details) {
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public HttpStatus getResponseCode() {
        return httpStatus;
    }

    public void setResponseCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExceptionResponse that = (ExceptionResponse) o;
        return httpStatus.equals(that.httpStatus) &&
                details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpStatus, details);
    }
}