package com.accenture.test.accenturetestchallenge.application.exception;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({RuntimeException.class})
  protected ResponseEntity<ApiError> handleRuntimeException(RuntimeException baseException) {
    var apiError =
        ApiError.builder()
            .timeStamp(ZonedDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .error(RuntimeException.class.getSimpleName())
            .message(baseException.getMessage())
            .build();

    return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ApiError> handleValidationException(WebExchangeBindException ex) {
    String errors = ex.getFieldErrors().stream()
            .map(err -> String.format("Field '%s' %s", err.getField(), err.getDefaultMessage()))
            .collect(Collectors.joining("; "));

    var apiError =
            ApiError.builder()
                    .timeStamp(ZonedDateTime.now())
                    .status(HttpStatus.BAD_REQUEST)
                    .error(WebExchangeBindException.class.getSimpleName())
                    .message(errors)
                    .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethoArgumentNotValidExceptions(
      MethodArgumentNotValidException ex) {

    var apiError =
        ApiError.builder()
            .timeStamp(ZonedDateTime.now())
            .status(HttpStatus.BAD_REQUEST)
            .error(RuntimeException.class.getSimpleName())
            .message(ex.getMessage())
            .build();
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }
}
