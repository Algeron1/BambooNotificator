package com.rgs.bamboonotifier.config;

import com.rgs.bamboonotifier.Entity.User;
import com.rgs.bamboonotifier.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class BearerCookieAuthFilter extends OncePerRequestFilter {

    private final UserService userService;

    public BearerCookieAuthFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("app-token".equals(cookie.getName())) {
                        try {
                            User user = userService.getUserByToken(cookie.getValue()).orElse(null);
                            if (user != null && "ADMIN".equals(user.getRole())) {
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        user.getUsername(), null,
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                );
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }
}
