package com.fixit_ops_management.infraestructure.configuration.exceptionhandler;

import com.fixit_ops_management.domain.exceptions.TechnicianAlreadyExistsException;
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
            TechnicianAlreadyExistsException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponse(
                exception.getMessage(),HttpStatus.CONFLICT.getReasonPhrase(), LocalDateTime.now(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handlerArgumentInvalidException(MethodArgumentNotValidException exception) {
        FieldError firstFieldError = exception.getFieldErrors().get(0);
        return ResponseEntity.badRequest().body(new ExceptionResponse(firstFieldError.getDefaultMessage(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now(), HttpStatus.BAD_REQUEST.value()));
    }

}
