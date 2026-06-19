package com.example.addressservice.Handler;

import com.example.addressservice.Config.Error;
import com.example.addressservice.ExceptionHandling.NoResourceFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(value = NoResourceFound.class)
    public ResponseEntity<Error> NoResourceFound(NoResourceFound e) {
         Error error=Error.builder().message(e.getMessage()).status(HttpStatus.NOT_FOUND).build();

         return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);

    }

}
