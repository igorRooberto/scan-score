package com.project.ScanScoreJava.infrastructure.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequestExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad Request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    // 2. Handles internal server errors (e.g., OpenCV memory crash, C++ errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericExceptions(Exception ex, HttpServletRequest request) {
        log.error("Internal Server Error: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred while processing the exam image.", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String message, String path) {

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

}
