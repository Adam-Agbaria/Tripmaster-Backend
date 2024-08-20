package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class CustomBadRequestException extends RuntimeException {

	private static final long serialVersionUID = -8732315531678788291L;

	public CustomBadRequestException(String message) {
        super(message);
    }
}

