package com.dhrubok.taskmaster.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private HttpStatus status;
    private boolean success;
    private String message;
    private Object payload;
    private Map<String, String> validationErrors;

    public Response(HttpStatus status, boolean success, String message) {
        this.status = status;
        this.success = success;
        this.message = message;
    }

    public Response(HttpStatus httpStatus, boolean equals, String message, Object payload) {
        this.status = httpStatus;
        this.success = equals;
        this.message = message;
        this.payload = payload;
    }

    public static Response getResponseEntity(boolean success, String message, Object payload) {
        return success
                ? getResponseEntity(HttpStatus.OK, message, payload)
                : getResponseEntity(HttpStatus.BAD_REQUEST, message, payload);
    }

    public static Response getResponseEntity(boolean success, String message) {
        return success
                ? getResponseEntity(HttpStatus.OK, message)
                : getResponseEntity(HttpStatus.BAD_REQUEST, message);
    }

    public static Response getResponseEntity(HttpStatus httpStatus, String message, Object payload) {
        return new Response(httpStatus, httpStatus.equals(HttpStatus.OK), message, payload);
    }

    public static Response getResponseEntity(HttpStatus httpStatus, String message) {
        return new Response(httpStatus, httpStatus.equals(HttpStatus.OK), message, null);
    }

    public static Response getSuccessResponseEntity(String message) {
        return getSuccessResponseEntity(message, null);
    }

    public static Response getSuccessResponseEntity(String message, Object payload) {
        return getResponseEntity(HttpStatus.OK, message, payload);
    }
}
