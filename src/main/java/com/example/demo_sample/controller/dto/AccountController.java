package com.example.demo_sample.controller.dto;

import com.example.demo_sample.domain.AccountEntity;
import com.example.demo_sample.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;

    // --- Register ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(accountService.register(request.email(), request.password()));
    }

    // --- Login ---
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

    // --- Refresh token ---
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        try {
            String newAccessToken = accountService.refreshAccessToken(body.get("refreshToken"));
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
        }
    }

    // --- Logout ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            accountService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("Token không tồn tại hoặc sai định dạng"));
    }

    // --- Get all accounts (Admin) ---
    @GetMapping
    public ResponseEntity<List<AccountEntity>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // --- Delete account (Admin only) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Bạn chưa đăng nhập"));
        }

        // Kiểm tra role
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).body(new ErrorResponse("Bạn không có quyền xóa tài khoản"));
        }

        accountService.deleteAccount(id);
        return ResponseEntity.ok(Map.of("message", "Xóa tài khoản User thành công"));
    }

    // --- DTO record ---
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String message, String email, String accessToken, String refreshToken) {}
    public record ErrorResponse(String error) {}
}
