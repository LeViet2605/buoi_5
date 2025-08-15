package com.example.demo_sample.util;

import com.example.demo_sample.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Bỏ qua: cho login/register đi thẳng
        String path = request.getServletPath();
        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 2) Thiếu hoặc sai định dạng token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "Token không tồn tại hoặc sai định dạng");
            return;
        }

        jwt = authHeader.substring(7);

        // 3) Token bị blacklist
        if (tokenBlacklist.contains(jwt)) {
            sendError(response, "Token đã bị vô hiệu hóa");
            return;
        }

        // 4) Giải mã token lấy username
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            sendError(response, "Token sai hoặc không hợp lệ");
            return;
        }

        // 4) Nếu có username nhưng chưa xác thực
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                sendError(response, "ID sai hoặc không tồn tại");
                return;
            } catch (Exception e) {
                sendError(response, "Lỗi khi tải thông tin người dùng");
                return;
            }

            // 6) Xác thực token
            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                sendError(response, "Token không hợp lệ hoặc đã hết hạn");
                return;
            }
        }

        // 7) Tiếp tục filter chain
        filterChain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        String p = path != null ? path.replaceAll("/$", "") : "";
        return p.equals("/api/account/login") || p.equals("/api/account/register");
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
