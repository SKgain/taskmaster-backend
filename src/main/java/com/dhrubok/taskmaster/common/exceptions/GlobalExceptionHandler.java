package com.dhrubok.taskmaster.common.exceptions;

import com.dhrubok.taskmaster.common.constants.ErrorCode;
import com.dhrubok.taskmaster.common.models.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // HANDLE VALIDATION ERRORS (@Valid, @NotNull, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        // GET FIRST ERROR MESSAGE (CUSTOM MESSAGE)
        String ErrorMessage = validationErrors.values().iterator().next();

        Response response = new Response(
                HttpStatus.BAD_REQUEST,
                Boolean.FALSE,
                ErrorMessage
        );
        response.setValidationErrors(validationErrors);
        response.setPayload(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // HANDLE RESOURCE NOT FOUND (Custom Exception)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        Response response = new Response(
                HttpStatus.NOT_FOUND,
                Boolean.FALSE,
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // HANDLE BAD CREDENTIALS (Login Failed)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> handleBadCredentialsException() {

        Response response = new Response(
                HttpStatus.UNAUTHORIZED,
                Boolean.FALSE,
                ErrorCode.ERROR_EMAIL_OR_PASSWORD_INCORRECT
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // HANDLE DUPLICATE ENTRY (Custom Exception)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Response> handleDuplicateResourceException(
            DuplicateResourceException ex,
            WebRequest request) {

        Response response = new Response(
                HttpStatus.CONFLICT,
                Boolean.FALSE,
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // HANDLE UNAUTHORIZED ACCESS (Custom Exception)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response> handleUnauthorizedException(
            UnauthorizedException ex) {

        Response response = new Response(
                HttpStatus.FORBIDDEN,
                Boolean.FALSE,
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // HANDLE ILLEGAL ARGUMENT EXCEPTION
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        Response response = new Response(
                HttpStatus.BAD_REQUEST,
                Boolean.FALSE,
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // HANDLE ALL OTHER EXCEPTIONS (Catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGlobalException(
            Exception ex) {

        Response response = new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                Boolean.FALSE,
                 ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
