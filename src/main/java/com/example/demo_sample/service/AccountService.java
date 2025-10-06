package com.example.demo_sample.service;

import com.example.demo_sample.Common.ApiResponse;
import com.example.demo_sample.Common.CommonErrorCode;
import com.example.demo_sample.domain.AccountEntity;
import com.example.demo_sample.repository.AccountRepository;
import com.example.demo_sample.util.JwtUtil;
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
            return ApiResponse.error(CommonErrorCode.EMAIL_EMPTY, 400);
        if (password == null || password.isBlank())
            return ApiResponse.error(CommonErrorCode.PASSWORD_EMPTY, 400);
        if (accountRepository.findByEmail(email).isPresent())
            return ApiResponse.error(CommonErrorCode.EMAIL_EXISTS, 409);

        AccountEntity account = new AccountEntity();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setRole("ROLE_USER");
        accountRepository.save(account);

        return ApiResponse.successCreated(CommonErrorCode.REGISTER_SUCCESS, Map.of("email", email));
    }

    // --- Login ---
    public ResponseEntity<?> login(String email, String password) {
        var acc = accountRepository.findByEmail(email);
        if (acc.isEmpty())
            return ApiResponse.error(CommonErrorCode.ACCOUNT_NOT_FOUND, 404);

        if (loginAttemptService.isBlocked(email))
            return ApiResponse.error(CommonErrorCode.ACCOUNT_LOCKED, 403);

        try {
            AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            loginAttemptService.loginSucceeded(email);

            String accessToken = jwtUtil.generateAccessToken(email);
            String refreshToken = jwtUtil.generateRefreshToken(email);

            AccountEntity account = acc.get();

            // ✅ Dùng LinkedHashMap để giữ thứ tự key
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("id", account.getId());
            responseData.put("email", email);
            responseData.put("accessToken", accessToken);
            responseData.put("refreshToken", refreshToken);

            return ApiResponse.success(CommonErrorCode.LOGIN_SUCCESS, responseData);

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            return ApiResponse.error(CommonErrorCode.BAD_CREDENTIALS, 401);
        } catch (Exception e) {
            return ApiResponse.error(CommonErrorCode.INTERNAL_ERROR, 500);
        }
    }

    // --- Refresh Token ---
    public ResponseEntity<?> refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            return ApiResponse.error(CommonErrorCode.REFRESH_TOKEN_EMPTY, 400);
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            if (!jwtUtil.isTokenValid(refreshToken, username))
                return ApiResponse.error(CommonErrorCode.REFRESH_TOKEN_INVALID, 401);

            String newAccessToken = jwtUtil.generateAccessToken(username);
            return ApiResponse.success(CommonErrorCode.LOGIN_SUCCESS, Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ApiResponse.error(CommonErrorCode.REFRESH_TOKEN_INVALID, 401);
        }
    }

    // --- Logout ---
    public ResponseEntity<?> logout(String token) {
        if (token == null || token.isBlank())
            return ApiResponse.error(CommonErrorCode.TOKEN_EMPTY, 400);

        tokenBlacklist.add(token);
        return ApiResponse.success(CommonErrorCode.LOGOUT_SUCCESS, null);
    }

    // --- Update account ---
    public ResponseEntity<?> updateAccount(Long id, String email, String rawPassword) {
        Optional<AccountEntity> optAcc = accountRepository.findById(id);
        if (optAcc.isEmpty())
            return ApiResponse.error(CommonErrorCode.USER_NOT_FOUND, 404);

        AccountEntity account = optAcc.get();
        account.setEmail(email);
        if (rawPassword != null && !rawPassword.isBlank()) {
            account.setPassword(passwordEncoder.encode(rawPassword));
        }
        accountRepository.save(account);
        return ApiResponse.success(CommonErrorCode.UPDATE_ACCOUNT_SUCCESS,
                Map.of("id", id, "email", email));
    }

    // --- Get by ID ---
    public ResponseEntity<?> getAccount(Long id) {
        return accountRepository.findById(id)
                .<ResponseEntity<?>>map(acc -> ApiResponse.success(CommonErrorCode.LOGIN_SUCCESS, acc))
                .orElseGet(() -> ApiResponse.error(CommonErrorCode.USER_NOT_FOUND, 404));
    }

    // --- Get all accounts (chỉ admin) ---
    public ResponseEntity<?> getAllAccountsResponse(Authentication authentication) {
        if (authentication == null) {
            return ApiResponse.error(CommonErrorCode.UNAUTHORIZED, 401);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ApiResponse.error(CommonErrorCode.FORBIDDEN, 403);
        }

        List<AccountEntity> accounts = accountRepository.findAll();
        return ApiResponse.success(CommonErrorCode.ALL_ACCOUNT, accounts);
    }


    // --- Delete account ---
    public ResponseEntity<?> deleteAccountResponse(Long id, Authentication authentication) {
        if (authentication == null)
            return ApiResponse.error(CommonErrorCode.UNAUTHORIZED, 401);

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!isAdmin)
            return ApiResponse.error(CommonErrorCode.FORBIDDEN, 403);

        if (!accountRepository.existsById(id))
            return ApiResponse.error(CommonErrorCode.USER_NOT_FOUND, 404);

        accountRepository.deleteById(id);
        return ApiResponse.success(CommonErrorCode.DELETE_ACCOUNT_SUCCESS, null);
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
