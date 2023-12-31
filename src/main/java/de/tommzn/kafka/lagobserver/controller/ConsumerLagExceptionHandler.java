package de.tommzn.kafka.lagobserver.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

@ControllerAdvice
public class ConsumerLagExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { ExecutionException.class, InterruptedException.class })
    protected ResponseEntity<Object> handleConflict(
      RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();;
        return handleExceptionInternal(ex, bodyOfResponse, 
          new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}