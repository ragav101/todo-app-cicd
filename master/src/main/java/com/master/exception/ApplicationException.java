package com.master.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

    private final int statusCode;

    public ApplicationException(String message, HttpStatus status) {
        super(message);
        this.statusCode = status.value();
    }
}
