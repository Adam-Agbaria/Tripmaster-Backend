package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class CustomInternalErrorException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 9058255094187850516L;

	public CustomInternalErrorException(String message) {
        super(message);
    }
}
