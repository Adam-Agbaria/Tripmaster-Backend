package com.example.app.util;

import com.example.app.exception.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.exception.message.detail}")
    private boolean detailedExceptionMessage;

    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(CustomBadRequestException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(CustomNotFoundException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(CustomConflictException.class)
    public ResponseEntity<?> handleConflictException(CustomConflictException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), ex, request);
    }

    @ExceptionHandler(CustomInternalErrorException.class)
    public ResponseEntity<?> handleInternalServerErrorException(CustomInternalErrorException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex, request);
    }

    private ResponseEntity<?> buildResponseEntity(HttpStatus status, String message, Exception ex, WebRequest request) {
        if (detailedExceptionMessage) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", status.value());
            body.put("error", status.getReasonPhrase());
            body.put("message", ex.getMessage());
            body.put("trace", getStackTraceAsString(ex));
            body.put("path", request.getDescription(false).substring(4));
            return new ResponseEntity<>(body, status);
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", status.value());
            body.put("error", status.getReasonPhrase());
            body.put("message", ex.getMessage());
            body.put("path", request.getDescription(false).substring(4));
            return new ResponseEntity<>(body, status);
        }
    }
    
    private String formatStackTrace(StackTraceElement[] stackTrace) {
        return Stream.of(stackTrace)
                .map(element -> element.toString())
                .collect(Collectors.joining("\n"));
    }
    
    private String getStackTraceAsString(Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        return stringWriter.toString();
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            String fieldName = ife.getPath().stream()
                .map(ref -> ref.getFieldName())
                .reduce((first, second) -> second)
                .orElse("unknown field");
            String errorMessage = constructCustomErrorMessage(fieldName, ife.getValue());
            return buildResponseEntity(HttpStatus.BAD_REQUEST, errorMessage, ex, request);
        }
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Invalid request message", ex, request);
    }
    
    private String constructCustomErrorMessage(String fieldName, Object rejectedValue) {
        switch (fieldName) {
            case "role":
                return "Invalid user role: " + rejectedValue;
            case "username":
                return "Invalid username: User username cannot be null.";
            case "email":
                return "Invalid email format: " + rejectedValue;
            case "avatar":
                return "Invalid avatar: User avatar cannot be null.";
            case "type":
                return "Invalid type: Type cannot be null or empty.";
            case "alias":
                return "Invalid alias: Alias cannot be null or empty.";
            case "location":
                return "Invalid location: Location cannot be null.";
            case "active":
                return "Invalid active status: Active status cannot be null.";
            case "objectId":
                return "Invalid ObjectId: ObjectId cannot be null.";
            case "superapp":
                return "Invalid superapp value: Superapp within ObjectId cannot be null or empty.";
            case "id":
                return "Invalid ID value: Id within ObjectId cannot be null or empty.";
            case "creationTimestamp":
                return "Invalid creation timestamp: CreationTimestamp cannot be null.";
            case "createdBy":
                return "Invalid createdBy: CreatedBy cannot be null.";
            case "userId":
                return "Invalid userId: UserId within CreatedBy cannot be null.";
            case "command":
                return "Invalid command: Command can't be null or empty.";
            case "commandId":
                return "Invalid commandId: CommandId cannot be null.";
            case "miniapp":
                return "Invalid miniapp: Miniapp name cannot be null or empty.";
            case "invocationTimestamp":
                return "Invalid invocation timestamp: Invocation timestamp cannot be null.";
            default:
                throw new CustomBadRequestException("Invalid value for " + fieldName + ": " + rejectedValue);
        }
    }
}
