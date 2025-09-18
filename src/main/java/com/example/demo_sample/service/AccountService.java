package com.example.demo_sample.service;

import com.example.demo_sample.domain.AccountEntity;
import com.example.demo_sample.repository.AccountRepository;
import com.example.demo_sample.JwtUtil;
import com.example.demo_sample.util.LoginAttemptService;
import com.example.demo_sample.util.TokenBlacklist;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public AccountEntity register(String email, String rawPassword) {
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("Email không được để trống");
        if (rawPassword == null || rawPassword.isEmpty()) throw new IllegalArgumentException("Password không được để trống");

        AccountEntity account = new AccountEntity();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setRole("ROLE_USER"); // mặc định
        return accountRepository.save(account);
    }

    // --- Login ---
    public Map<String, String> login(String email, String rawPassword, AuthenticationManager authenticationManager) {
        // Kiểm tra xem user có bị khóa không
        if (loginAttemptService.isBlocked(email)) {
            throw new RuntimeException("Tài khoản bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau 1 phút.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword)
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Nếu đăng nhập thành công thì reset số lần thử
            loginAttemptService.loginSucceeded(email);

            String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            tokens.put("email", userDetails.getUsername());
            return tokens;
        } catch (BadCredentialsException ex) {
            // Nếu sai mật khẩu thì tăng số lần thử
            loginAttemptService.loginFailed(email);
            throw ex; // vẫn ném để controller xử lý
        }
    }


    // --- Refresh Token ---
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh Token không được để trống");
        }

        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Refresh Token không hợp lệ");
        }

        if (!jwtUtil.isTokenValid(refreshToken, username)) {
            throw new IllegalArgumentException("Refresh Token không hợp lệ hoặc đã hết hạn");
        }

        UserDetails userDetails = loadUserByUsername(username);
        return jwtUtil.generateAccessToken(userDetails.getUsername());
    }

    // --- Logout ---
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token không tồn tại");
        }
        tokenBlacklist.add(token);
    }

    // --- Update ---
    public AccountEntity updateAccount(Long id, String email, String rawPassword) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        account.setEmail(email);
        if (rawPassword != null && !rawPassword.isEmpty()) {
            account.setPassword(passwordEncoder.encode(rawPassword));
        }
        return accountRepository.save(account);
    }

    // --- Delete ---
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy user");
        }
        accountRepository.deleteById(id);
    }

    // --- Get by ID ---
    public AccountEntity getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // --- Get all ---
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
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
