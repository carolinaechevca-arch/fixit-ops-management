package com.fixit_ops_management.infraestructure.configuration.exceptionhandler;

import com.fixit_ops_management.domain.exceptions.*;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvisor {

    @ExceptionHandler(TechnicianAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handlerTechnicianAlreadyExistsException(
            TechnicianAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponse(
                exception.getMessage(), HttpStatus.CONFLICT.getReasonPhrase(), LocalDateTime.now(),
                HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handlerArgumentInvalidException(
            MethodArgumentNotValidException exception) {
        FieldError firstFieldError = exception.getFieldErrors().get(0);
        return ResponseEntity.badRequest().body(new ExceptionResponse(firstFieldError.getDefaultMessage(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleTaskNotFoundException(TaskNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(
                exception.getMessage(), HttpStatus.NOT_FOUND.getReasonPhrase(), LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(TaskCannotBeDeletedException.class)
    public ResponseEntity<ExceptionResponse> handleTaskCannotBeDeletedException(
            TaskCannotBeDeletedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(
                exception.getMessage(), HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(NoMasterTechniciansAvailableException.class)
    public ResponseEntity<ExceptionResponse> handleNoMasterTechniciansAvailableException(
            NoMasterTechniciansAvailableException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponse(
                exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(TaskNotUrgentException.class)
    public ResponseEntity<ExceptionResponse> handleTaskNotUrgentException(TaskNotUrgentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponse(
                exception.getMessage(), HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()));
    }

}
