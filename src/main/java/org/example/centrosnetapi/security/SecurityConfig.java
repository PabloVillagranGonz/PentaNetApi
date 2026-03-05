package org.example.centrosnetapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // Si no existe en properties → usa localhost por defecto
    @Value("${app.allowed-origins:http://localhost:5173}")
    private String allowedOriginsProperty;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // API stateless (JWT)
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth pública
                        .requestMatchers("/auth/**").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/doc/**"
                        ).permitAll()

                        // ESPACIOS
                        .requestMatchers(HttpMethod.GET, "/api/espacios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "ALUMNO")

                        .requestMatchers(HttpMethod.POST, "/api/espacios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.PUT, "/api/espacios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.DELETE, "/api/espacios/**")
                        .hasRole("ADMIN")

                        // RESERVAS
                        .requestMatchers(HttpMethod.GET, "/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "ALUMNO")

                        .requestMatchers(HttpMethod.POST, "/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/**")
                        .hasRole("ADMIN")

                        // CORREOS
                        .requestMatchers("/api/correos/**")
                        .hasAnyRole("ADMIN", "ALUMNO", "PROFESOR", "SECRETARIA")

                        // CENTERS - lectura
                        .requestMatchers(HttpMethod.GET, "/api/centers/**")
                        .hasAnyRole("ADMIN","SECRETARIA","ALUMNO","PROFESOR")

                        // ADMIN GLOBAL
                        .requestMatchers(
                                "/admin/**",
                                "/api/courses/**",
                                "/api/course-subjects/**",
                                "/api/centers/**",
                                "/api/instruments/**"
                        ).hasRole("ADMIN")

                        // Todo lo demás requiere login
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ======================
    // 401 - No autenticado
    // ======================
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");
    }

    // ======================
    // 403 - Sin permisos
    // ======================
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Sin permisos");
    }

    // ======================
    // Password Encoder
    // ======================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ======================
    // CORS
    // ======================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*")); // 👈 CLAVE
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}