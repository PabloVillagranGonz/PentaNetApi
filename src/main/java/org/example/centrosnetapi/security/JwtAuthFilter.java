package org.example.centrosnetapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.UserRepository;
import org.example.centrosnetapi.services.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        System.out.println("➡️ REQUEST: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("➡️ AUTH HEADER: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ NO TOKEN");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            System.out.println("❌ TOKEN INVALIDO");
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        System.out.println("✅ TOKEN EMAIL: " + email);

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            System.out.println("👤 USER ROLE: " + user.getRole());

            user.getAuthorities().forEach(a ->
                    System.out.println("🔐 AUTHORITY: " + a.getAuthority())
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}