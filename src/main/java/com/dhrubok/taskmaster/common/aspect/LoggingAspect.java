package com.dhrubok.taskmaster.common.aspect;

import com.dhrubok.taskmaster.common.annotations.ApiLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private final ObjectMapper mapper =  new ObjectMapper();

    @Around("@annotation(com.dhrubok.taskmaster.common.annotations.ApiLog)")
    public Object ApiLog (ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ApiLog apiLog = method.getAnnotation(ApiLog.class);

        String apiPath = getApiPath(method);
        String httpMethod = getHttpMethod(method);
        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("========== API CALL START ==========");
        log.info("Timestamp: {}", timestamp);
        log.info("HTTP Method: {}", httpMethod);
        log.info("API Path: {}", apiPath);
        log.info("Method Name: {}", methodName);
        log.info("Description: {}", apiLog.value());
        log.info("Request Parameters: {}", Arrays.toString(args));

        if (args.length > 0) {
            try {
                for (Object arg : args) {
                    if (arg != null && !isPrimitiveOrWrapper(arg.getClass())) {
                        log.info("Request Body: {}", mapper.writeValueAsString(arg));
                    }
                }
            } catch (Exception e) {
                log.warn("Could not serialize request body", e);
            }
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Execution Time: {} ms", executionTime);

            if (exception != null) {
                log.error("Response Status: FAILED");
                log.error("Error: {}", exception.getMessage());
            } else {
                log.info("Response Status: SUCCESS");
                try {
                    if (result != null) {
                        log.info("Response: {}", mapper.writeValueAsString(result));
                    }
                } catch (Exception e) {
                    log.warn("Could not serialize response", e);
                }
            }

            log.info("========== API CALL END ==========\n");
        }
    }

    private String getApiPath(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            return Arrays.toString(mapping.value().length > 0 ? mapping.value() : mapping.path());
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            return Arrays.toString(mapping.value().length > 0 ? mapping.value() : mapping.path());
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            return Arrays.toString(mapping.value().length > 0 ? mapping.value() : mapping.path());
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            return Arrays.toString(mapping.value().length > 0 ? mapping.value() : mapping.path());
        }
        return "Unknown";
    }

    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        return "UNKNOWN";
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Double.class) ||
                type.equals(Float.class) ||
                type.equals(Boolean.class);
    }
}