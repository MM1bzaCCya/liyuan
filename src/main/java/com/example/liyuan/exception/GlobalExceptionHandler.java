package com.example.liyuan.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "msg", "服务器内部错误: " + e.getMessage()
        ));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", e.getCode(),
                "msg", e.getMessage()
        ));
    }
}