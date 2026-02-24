package org.example.centrosnetapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 🔐 Manejo claro de errores
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth

                        // 🔥 CORS PREFLIGHT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔓 AUTH
                        .requestMatchers("/auth/**").permitAll()

                        // 🔓 SWAGGER
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/doc/**"
                        ).permitAll()

                        // =====================================================
                        // 🎹 AULAS
                        // =====================================================
                        .requestMatchers(HttpMethod.GET, "/api/aulas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "STUDENT")

                        .requestMatchers(HttpMethod.POST, "/api/aulas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.PUT, "/api/aulas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.DELETE, "/api/aulas/**")
                        .hasRole("ADMIN")

                        // =====================================================
                        // 🕒 RESERVAS
                        // =====================================================
                        .requestMatchers(HttpMethod.GET, "/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "STUDENT")

                        .requestMatchers(HttpMethod.POST, "/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/**")
                        .hasRole("ADMIN")

                        // =====================================================
                        // 👤 USUARIOS - BÚSQUEDA POR EMAIL
                        // =====================================================
                        .requestMatchers(HttpMethod.GET, "/api/users/email")
                        .hasAnyRole("ADMIN", "SECRETARIA", "TEACHER", "STUDENT")

                        // =====================================================
                        // 👤 USUARIOS - GESTIÓN
                        // =====================================================
                        .requestMatchers("/api/users/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        // =====================================================
                        // 📧 CORREOS
                        // =====================================================
                        .requestMatchers("/api/correos/**")
                        .hasAnyRole("ADMIN", "STUDENT", "TEACHER", "SECRETARIA")

                        // =====================================================
                        // 📚 SUBJECTS
                        // =====================================================

                        // 👨‍🏫 PROFESOR → sus asignaturas
                        .requestMatchers(HttpMethod.GET, "/api/subjects/mine")
                        .hasRole("TEACHER")

                        // 🔐 ADMIN → gestión completa
                        .requestMatchers("/api/subjects/**")
                        .hasRole("ADMIN")

                        // =====================================================
                        // 🔐 ADMIN GENERAL
                        // =====================================================
                        .requestMatchers(
                                "/admin/**",
                                "/api/rooms/**",
                                "/api/courses/**",
                                "/api/course-subjects/**",
                                "/api/centers/**",
                                "/api/instruments/**"
                        ).hasRole("ADMIN")

                        // 🔒 RESTO
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================
    // 🔐 MANEJO DE ERRORES
    // =====================================================

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Sin permisos");
        };
    }

    // =====================================================
    // 🔐 PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // 🌍 CORS
    // =====================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}