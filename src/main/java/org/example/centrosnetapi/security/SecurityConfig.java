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
                .authorizeHttpRequests(auth -> auth

                        // 🔥 CORS PREFLIGHT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔓 AUTH
                        .requestMatchers("/auth/**").permitAll()

                        // 🔥 SWAGGER
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/doc/**"
                        ).permitAll()

                        // =====================================================
                        // 🎹 AULAS
                        // =====================================================

                        // 🔓 SOLO LECTURA (ADMIN, SECRETARIA, STUDENT)
                        .requestMatchers(HttpMethod.GET, "/api/aulas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA", "STUDENT")

                        // 🔐 CREAR / MODIFICAR (ADMIN, SECRETARIA)
                        .requestMatchers(HttpMethod.POST, "/api/aulas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        .requestMatchers(HttpMethod.PUT, "/api/aulas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        // 🔐 BORRAR (solo ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/aulas/**")
                        .hasRole("ADMIN")

                        // =====================================================
                        // 🕒 RESERVAS
                        // =====================================================

                        // 🔓 VER RESERVAS (ADMIN, SECRETARIA)
                        .requestMatchers(HttpMethod.GET, "/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        // 🔐 CREAR / FINALIZAR RESERVA (ADMIN, SECRETARIA)
                        .requestMatchers("/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        // =====================================================
                        // 👤 USUARIOS (búsqueda para reservas)
                        // =====================================================

                        .requestMatchers("/api/usuarios/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        // =====================================================
                        // 📧 CORREOS
                        // =====================================================

                        .requestMatchers("/api/correos/**")
                        .hasAnyRole("ADMIN", "STUDENT", "TEACHER", "SECRETARIA")

                        // =====================================================
                        // 🔐 ADMIN GENERAL
                        // =====================================================

                        .requestMatchers(
                                "/admin/**",
                                "/api/rooms/**",
                                "/api/subjects/**",
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}