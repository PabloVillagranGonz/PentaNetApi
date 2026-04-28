package org.example.centrosnetapi.security;

import lombok.RequiredArgsConstructor;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
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

                        // 1. ACCESO PÚBLICO TOTAL
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()

                        // 🔓 PERMITIMOS VER CENTROS SIN TOKEN (Público)
                        // Esto soluciona el error 401 en la pantalla de info del centro
                        .requestMatchers(HttpMethod.GET, "/api/centers/**").permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/doc/**"
                        ).permitAll()

                        // 2. RUTAS PROTEGIDAS (Requieren Login)
                        .requestMatchers("/api/espacios/**").authenticated()
                        .requestMatchers("/api/reservas/**").authenticated()
                        .requestMatchers("/api/correos/**").authenticated()

                        // Protegemos la gestión de centros (crear/borrar), pero permitimos el GET arriba
                        .requestMatchers(HttpMethod.POST, "/api/centers/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/centers/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/centers/**").authenticated()

                        .requestMatchers("/api/cursos/**").authenticated()
                        .requestMatchers("/api/subjects/**").authenticated()
                        .requestMatchers("/api/teachers/**").authenticated()
                        .requestMatchers("/api/students/**").authenticated()
                        .requestMatchers("/api/asignaturas-curso/**").authenticated()
                        .requestMatchers("/api/sesiones/**").authenticated()
                        .requestMatchers("/api/evaluacion/**").authenticated()

                        // 3. CUALQUIER OTRA PETICIÓN
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==========================================
    // 🛡️ MANEJO DE ERRORES
    // ==========================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No autenticado\", \"message\": \"" + ex.getMessage() + "\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Sin permisos\", \"message\": \"No tienes autorización para acceder a este recurso\"}");
        };
    }

    // ==========================================
    // ⚙️ COMPONENTES ADICIONALES
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}