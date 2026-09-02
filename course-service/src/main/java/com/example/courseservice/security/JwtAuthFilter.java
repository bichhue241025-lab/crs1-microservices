package com.example.courseservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (
                authHeader != null
                        && authHeader.startsWith("Bearer ")
        ) {

            String token =
                    authHeader.substring(7);

            try {

                SecretKey key =
                        Keys.hmacShaKeyFor(
                                secret.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

                Claims claims =
                        Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                String username =
                        claims.getSubject();

                String role =
                        claims.get(
                                "role",
                                String.class
                        );

                // Buoi 9:
                // Doc them userId tu JWT
                Long userId =
                        claims.get(
                                "userId",
                                Long.class
                        );

                var authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,

                                // Buoi 9:
                                // Tam dung credentials de luu userId
                                userId,

                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

            } catch (Exception e) {

                // Token sai, het han hoac khong hop le
                SecurityContextHolder.clearContext();

                // Tra 401 de frontend Response Interceptor xu ly
                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.setContentType(
                        "application/json;charset=UTF-8"
                );

                response.getWriter().write(
                        "{\"message\":\"Token khong hop le hoac da het han\"}"
                );

                return;
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}