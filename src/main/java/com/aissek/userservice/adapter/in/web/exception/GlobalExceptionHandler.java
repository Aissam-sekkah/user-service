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
        // problemDetail.setTitle("Email already exist" + exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
