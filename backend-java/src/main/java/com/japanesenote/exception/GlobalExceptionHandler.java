package com.japanesenote.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }

    // TODO: write explanation of this!!!
    // Spring will intercept any exception in app, and scans all @ControllerAdvice classes for matching @ExceptionHandler
    // So we donnot directly call GlobalExecptionHandler in our code.
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoteNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleArgumentInvalid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(400).body(errors);
    }
}