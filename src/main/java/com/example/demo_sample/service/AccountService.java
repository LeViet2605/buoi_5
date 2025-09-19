package com.example.demo_sample.service;

import com.example.demo_sample.domain.AccountEntity;
import com.example.demo_sample.repository.AccountRepository;
import com.example.demo_sample.JwtUtil;
import com.example.demo_sample.util.LoginAttemptService;
import com.example.demo_sample.util.TokenBlacklist;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final LoginAttemptService loginAttemptService;
    private final AuthenticationConfiguration authenticationConfiguration;

    // --- Rehash password khi DB chứa mật khẩu chưa mã hoá ---
    @PostConstruct
    public void rehashPasswords() {
        accountRepository.findAll().forEach(acc -> {
            if (!acc.getPassword().startsWith("$2a$")) {
                acc.setPassword(passwordEncoder.encode(acc.getPassword()));
                accountRepository.save(acc);
            }
        });
    }

    // --- Register ---
    public ResponseEntity<?> register(String email, String password) {
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Email không được để trống"));
        if (password == null || password.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Password không được để trống"));
        if (accountRepository.findByEmail(email).isPresent())
            return ResponseEntity.status(409).body(Map.of("error", "Email đã tồn tại"));

        AccountEntity account = new AccountEntity();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setRole("ROLE_USER");
        accountRepository.save(account);

        return ResponseEntity.status(201).body(Map.of("message", "Đăng ký thành công", "email", email));
    }

    // --- Login ---
    public ResponseEntity<?> login(String email, String password) {
        var optAcc = accountRepository.findByEmail(email);
        if (optAcc.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Tài khoản chưa tồn tại"));

        if (loginAttemptService.isBlocked(email))
            return ResponseEntity.status(403).body(Map.of("error", "Bạn nhập sai mật khẩu quá 3 lần. Tài khoản tạm thời bị khoá, vui lòng thử lại sau 1 phút"));

        try {
            AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            loginAttemptService.loginSucceeded(email);

            String accessToken = jwtUtil.generateAccessToken(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Đăng nhập thành công",
                    "email", email,
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            ));
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            return ResponseEntity.status(401).body(Map.of("error", "Email hoặc mật khẩu không đúng"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi hệ thống"));
        }
    }

    // --- Refresh Token ---
    public ResponseEntity<?> refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token trống"));
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            if (!jwtUtil.isTokenValid(refreshToken, username))
                return ResponseEntity.status(401).body(Map.of("error", "Refresh token không hợp lệ"));

            String newAccessToken = jwtUtil.generateAccessToken(username);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token không hợp lệ"));
        }
    }

    // --- Logout ---
    public ResponseEntity<?> logout(String token) {
        if (token == null || token.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Token không tồn tại"));

        tokenBlacklist.add(token);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    // --- Update account ---
    public ResponseEntity<?> updateAccount(Long id, String email, String rawPassword) {
        Optional<AccountEntity> optAcc = accountRepository.findById(id);
        if (optAcc.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy user"));

        AccountEntity account = optAcc.get();
        account.setEmail(email);
        if (rawPassword != null && !rawPassword.isBlank()) {
            account.setPassword(passwordEncoder.encode(rawPassword));
        }
        accountRepository.save(account);
        return ResponseEntity.ok(Map.of("message", "Cập nhật tài khoản thành công", "id", id, "email", email));
    }

    // --- Get by ID ---
    public ResponseEntity<?> getAccount(Long id) {
        return accountRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy user")));
    }

    // --- Get all accounts ---
    public ResponseEntity<?> getAllAccountsResponse() {
        List<AccountEntity> accounts = accountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }

    // --- Delete account ---
    public ResponseEntity<?> deleteAccountResponse(Long id, Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).body(Map.of("error", "Bạn chưa đăng nhập"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!isAdmin)
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xóa tài khoản"));

        if (!accountRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("error", "User không tồn tại"));

        accountRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Xóa tài khoản thành công"));
    }

    // --- Spring Security ---
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AccountEntity account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));
        return new org.springframework.security.core.userdetails.User(
                account.getEmail(),
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(account.getRole()))
        );
    }
}
