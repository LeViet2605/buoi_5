package com.example.demo_sample.config;

import com.example.demo_sample.domain.AccountEntity;
import com.example.demo_sample.repository.AccountRepository;
import com.example.demo_sample.util.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AccountRepository accountRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Xử lý khi chưa xác thực (401)
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Chưa xác thực hoặc token không hợp lệ");
            new ObjectMapper().writeValue(response.getWriter(), body);
        };
    }

    // Xử lý khi không đủ quyền (403)
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Tài khoản không có quyền thực hiện hành động này");
            new ObjectMapper().writeValue(response.getWriter(), body);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả đăng ký & login
                        .requestMatchers(HttpMethod.POST, "/api/account/register", "/api/account/login").permitAll()

                        // Chỉ ADMIN được xem & xoá account
                        .requestMatchers(HttpMethod.GET, "/api/account/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/account/**").hasRole("ADMIN")

                        // USER và ADMIN đều được xem/thêm task
                        .requestMatchers(HttpMethod.GET, "/api/tasks/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/**").hasAnyRole("USER", "ADMIN")

                        // Chỉ ADMIN được xoá task
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasRole("ADMIN")

                        // Các request khác cần login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint()) // chưa login / token sai
                        .accessDeniedHandler(accessDeniedHandler()) // có token nhưng không có quyền
                )
                .formLogin(form -> form.disable());

        return http.build();
    }

    // 🔥 Tạo admin mặc định khi app khởi động
    @Bean
    public CommandLineRunner initAdmin(PasswordEncoder passwordEncoder) {
        return args -> {
            if (accountRepository.findByEmail("admin@gmail.com").isEmpty()) {
                AccountEntity admin = new AccountEntity();
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("123456")); // mật khẩu mặc định
                admin.setRole("ROLE_ADMIN");
                accountRepository.save(admin);
                System.out.println("✅ Default admin created: admin@gmail.com / 123456");
            }
        };
    }
}
