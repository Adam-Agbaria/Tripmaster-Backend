package com.example.app.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class CustomNotFoundException extends RuntimeException {


	
	private static final long serialVersionUID = 7168863930708865750L;

	public CustomNotFoundException(String message) {
        super(message);
    }
}

