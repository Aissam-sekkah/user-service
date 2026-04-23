package com.aissek.userservice.adapter.in.web.exception;

import com.aissek.userservice.domain.service.UserDomainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserDomainService.UserEmailAlreadyExistsException.class)
    public ProblemDetail handleUserEmailExist(UserDomainService.UserEmailAlreadyExistsException exception){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UserDomainService.UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserDomainService.UserNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
