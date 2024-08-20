package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class CustomConflictException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6225094261426317714L;

	public CustomConflictException(String message) {
        super(message);
    }
}



