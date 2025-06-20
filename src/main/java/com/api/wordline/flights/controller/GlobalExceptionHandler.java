package com.api.wordline.flights.controller;

import com.api.wordline.flights.controller.dto.ErrorResponse;
import com.api.wordline.flights.exception.BusinessException;
import com.api.wordline.flights.exception.TechnicalException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponse> handleTechnicalException(TechnicalException ex) {
        ErrorResponse errorResponse = new ErrorResponse(List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    FieldError fieldError = (FieldError) error;
                    return fieldError.getDefaultMessage();
                })
                .collect(Collectors.toList());

        String combinedErrorMessage = String.join(", ", errorMessages);
        ErrorResponse errorResponse = new ErrorResponse(errorMessages);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> messages = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(messages);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
