package com.example.demo_sample.Common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;



public class ApiResponse {

    public static ResponseEntity<?> success(CommonErrorCode code, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code.getCode());
        body.put("message", code.getMessage());
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<?> successCreated(CommonErrorCode code, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code.getCode());
        body.put("message", code.getMessage());
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    public static ResponseEntity<?> error(CommonErrorCode code, int status) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code.getCode());
        body.put("message", code.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
