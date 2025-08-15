package com.example.demo_sample.controller.dto;

import com.example.demo_sample.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(accountService.register(request.email(), request.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Map<String, String> tokens = accountService.login(
                    request.email(), request.password(), authenticationManager
            );
            return ResponseEntity.ok(new LoginResponse(
                    "Đăng nhập thành công",
                    tokens.get("email"),
                    tokens.get("accessToken"),
                    tokens.get("refreshToken")
            ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new ErrorResponse("Email hoặc mật khẩu không đúng"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        try {
            String newAccessToken = accountService.refreshAccessToken(body.get("refreshToken"));
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
        }
    }

    // DTO record
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String message, String email, String accessToken, String refreshToken) {}
    public record ErrorResponse(String error) {}
}
