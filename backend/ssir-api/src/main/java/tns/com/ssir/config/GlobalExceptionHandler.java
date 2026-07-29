package tns.com.ssir.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j; // Import Lombok Logger
import tns.com.ssir.dto.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j // Automatically creates the 'log' object in your class
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage());

        List<String> details = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            if (fieldName.contains(".")) {
                fieldName = fieldName.substring(fieldName.lastIndexOf(".") + 1);
            }
            String errorMsg = fieldName + ": " + violation.getMessage();
            details.add(errorMsg);
            sb.append(errorMsg).append("; ");
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request (Validation Failed)")
                .message(sb.toString())
                .details(details)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request (Business Rule Violation)")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        // CRUCIAL: Print the full error stack trace to the console so we can see what crashed
        log.error("Internal Server Error occurred at " + request.getRequestURI(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please contact the system administrator.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex, 
            jakarta.servlet.http.HttpServletRequest request) {
        
        log.warn("Authentication failure at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value()) // Returns 401 instead of 500
                .error("Unauthorized")
                .message("Invalid username, email, or password.") // Secure, generic message
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}