package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class CustomNotAuthorizedOpperation extends RuntimeException {

    private static final long serialVersionUID = 8116424860101945440L;

    public CustomNotAuthorizedOpperation() {
        super("You don't have permission to do this operation");
    }

    public CustomNotAuthorizedOpperation(String message) {
        super(message);
    }

    public CustomNotAuthorizedOpperation(Throwable cause) {
        super(cause);
    }

    public CustomNotAuthorizedOpperation(String message, Throwable cause) {
        super(message, cause);
    }
}
