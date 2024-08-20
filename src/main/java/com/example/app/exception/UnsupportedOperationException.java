package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UnsupportedOperationException extends RuntimeException {

    private static final long serialVersionUID = 8116424860105945440L;

    public UnsupportedOperationException() {
        super("This operation is no longer supported. Please refer to the API documentation for current operations.");
    }

    public UnsupportedOperationException(String message) {
        super(message);
    }

    public UnsupportedOperationException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
