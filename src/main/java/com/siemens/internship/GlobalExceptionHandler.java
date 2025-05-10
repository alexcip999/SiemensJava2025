package com.siemens.internship;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles generic runtime exceptions.
     *
     * @param ex The exception that was thrown
     * @param request The web request during which the exception was thrown
     * @return ResponseEntity with error details and HTTP 500 status
     */

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        error.put("status", "error");

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles resource not found exceptions.
     *
     * @param ex The exception that was thrown
     * @param request The web request during which the exception was thrown
     * @return ResponseEntity with error details and HTTP 404 status
     */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        error.put("status", "not_found");

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}

/**
 * Custom exception for resource not found situations.
 */

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
