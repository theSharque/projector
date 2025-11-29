package com.projector.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidTokenException extends ResponseStatusException {

    public static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public InvalidTokenException(String message) {
        super(STATUS, message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(STATUS, message, cause);
    }
}
