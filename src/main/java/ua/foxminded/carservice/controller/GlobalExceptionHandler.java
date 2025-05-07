package ua.foxminded.carservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carservice.dto.ErrorResponse;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errorDetails = new HashMap<>();

    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errorDetails.put(error.getField(), error.getDefaultMessage()));

    return new ResponseEntity<>(ErrorResponse.builder()
        .status(400)
        .message("Request Validation Error")
        .errorDetails(errorDetails)
        .build(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(EntityNotFoundException ex) {
    return new ResponseEntity<>(ErrorResponse.builder()
        .status(404)
        .message("No Resource Found Exception")
        .build(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleObjectOptimisticLockingFailureException(
      ObjectOptimisticLockingFailureException ex) {
    if (ex.getCause() instanceof StaleObjectStateException) {
      return new ResponseEntity<>(ErrorResponse.builder()
          .status(404)
          .message("No Resource Found Exception")
          .build(), HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(ErrorResponse.builder()
        .status(500)
        .message("Internal Server Error")
        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(NotImplementedException.class)
  public ResponseEntity<ErrorResponse> handleNotImplementedException(NotImplementedException ex) {
    return new ResponseEntity<>(ErrorResponse.builder()
        .status(501)
        .message("Not Implemented")
        .build(), HttpStatus.NOT_IMPLEMENTED);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    if (ex.getCause() instanceof ConstraintViolationException) {
      ConstraintViolationException constraintViolationEx = (ConstraintViolationException) ex.getCause();
      if (constraintViolationEx.getCause() instanceof PSQLException) {
        PSQLException psqlEx = (PSQLException) constraintViolationEx.getCause();
        if ("23505".equals(psqlEx.getSQLState())) {
          return new ResponseEntity<>(ErrorResponse.builder()
              .status(409)
              .message("Resource Already Exists")
              .build(), HttpStatus.CONFLICT);
        }
        if ("23503".equals(psqlEx.getSQLState())) {
          return new ResponseEntity<>(ErrorResponse.builder()
              .status(409)
              .message("Resource Cannot Be Deleted Due To Dependencies")
              .build(), HttpStatus.CONFLICT);
        }
      }
    }
    return new ResponseEntity<>(ErrorResponse.builder()
        .status(500)
        .message("Database Error")
        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    return new ResponseEntity<>(ErrorResponse.builder()
        .status(500)
        .message("Internal Server Error")
        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
