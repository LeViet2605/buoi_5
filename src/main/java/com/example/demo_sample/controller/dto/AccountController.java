package com.example.demo_sample.controller.dto;

import com.example.demo_sample.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        return accountService.register(request.email(), request.password());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return accountService.login(request.email(), request.password());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        return accountService.refreshAccessToken(body.get("refreshToken"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
        return accountService.logout(token);
    }

//    chưa dùng
    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        return accountService.getAllAccountsResponse();
    }

//    chưa dùng
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

//     chưa dùng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @RequestBody UpdateRequest request) {
        return accountService.updateAccount(id, request.email(), request.password());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, Authentication authentication) {
        return accountService.deleteAccountResponse(id, authentication);
    }


    // --- DTOs ---
    public record LoginRequest(String email, String password) {}
    public record UpdateRequest(String email, String password) {}
}
