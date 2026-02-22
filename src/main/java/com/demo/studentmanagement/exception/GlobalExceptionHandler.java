package com.demo.studentmanagement.exception;

import com.demo.studentmanagement.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(StudentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleStudentNotFound(
      StudentNotFoundException ex, HttpServletRequest request) {
    log.warn("Student not found: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateEmail(
      DuplicateEmailException ex, HttpServletRequest request) {
    log.warn("Duplicate email: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    log.warn("Invalid argument: {}", ex.getMessage());
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error: ", ex);
    ErrorResponse error =
        ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
