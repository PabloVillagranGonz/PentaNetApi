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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

                        .requestMatchers("/api/reservas/**")
                        .hasAnyRole("ADMIN", "SECRETARIA")

                        // =====================================================
                        // 👤 USUARIOS
                        // =====================================================

                        .requestMatchers("/api/usuarios/**")
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

                        // 🔐 ADMIN → gestión completa de asignaturas
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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