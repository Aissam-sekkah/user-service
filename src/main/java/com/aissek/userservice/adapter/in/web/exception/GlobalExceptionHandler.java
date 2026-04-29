package com.aissek.userservice.adapter.in.web.exception;

import com.aissek.userservice.domain.service.UserDomainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserDomainService.UserEmailAlreadyExistsException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleUserEmailExist(UserDomainService.UserEmailAlreadyExistsException exception){
        return org.springframework.http.ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage()));
    }

    @ExceptionHandler(UserDomainService.UserNotFoundException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleUserNotFound(UserDomainService.UserNotFoundException exception) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(UserDomainService.InvalidPasswordException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleInvalidPassword(UserDomainService.InvalidPasswordException exception) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(UserDomainService.AuthenticationFailedException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleAuthenticationFailed(UserDomainService.AuthenticationFailedException exception) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException exception) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail));
    }
}
