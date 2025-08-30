package com.foxfox.demo.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String,Object>> body(HttpStatus status, String message){
        Map<String,Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        return ResponseEntity.status(status).body(map);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String,Object>> handleIllegal(RuntimeException ex){
        String msg = ex.getMessage();
        if(msg == null || msg.isBlank()){
            msg = "请求不合法";
        }
        // 统一将与非学生相关的异常提示标准化
        if(msg.contains("只能添加学生") || msg.contains("只能移动学生")){
            msg = "只能添加/移动学生用户";
        }
        return body(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(EntityNotFoundException ex){
        return body(HttpStatus.NOT_FOUND, ex.getMessage()==null?"资源不存在":ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleOther(Exception ex){
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "服务器错误");
    }
}
