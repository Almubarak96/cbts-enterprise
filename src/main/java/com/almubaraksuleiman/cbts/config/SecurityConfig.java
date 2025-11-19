//
//package com.almubaraksuleiman.cbts.config;
//
//import com.almubaraksuleiman.cbts.security.authentication.*;
//import com.almubaraksuleiman.cbts.security.authentication.manager.AuthenticationManagerImpl;
//import com.almubaraksuleiman.cbts.security.authentication.providers.AdminAuthenticationProvider;
//import com.almubaraksuleiman.cbts.security.authentication.providers.ExaminerAuthenticationProvider;
//import com.almubaraksuleiman.cbts.security.authentication.providers.ProctorAuthenticationProvider;
//import com.almubaraksuleiman.cbts.security.authentication.providers.StudentAuthenticationProvider;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//import java.util.List;
//
///**
// * @author Almubarak Suleiman
// * @version 1.0
// * @since 2025
// **/
//
//@Configuration
//@EnableWebSecurity
//@NoArgsConstructor
//@AllArgsConstructor
//public class SecurityConfig {
//
//    @Autowired
//    private AdminAuthenticationProvider adminAuthenticationProvider;
//
//    @Autowired
//    private ExaminerAuthenticationProvider examinerAuthenticationProvider;
//
//    @Autowired
//    private StudentAuthenticationProvider studentAuthenticationProvider;
//
//    @Autowired
//    private ProctorAuthenticationProvider proctorAuthenticationProvider;
//
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//
//
//
//    @Bean
//    public AuthenticationManagerImpl authenticationManager() {
//        return new AuthenticationManagerImpl(List.of(
//                adminAuthenticationProvider,
//                examinerAuthenticationProvider,
//                studentAuthenticationProvider,
//                proctorAuthenticationProvider
//        ));
//    }
//
//
//
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/**").permitAll()
//                .anyRequest().authenticated()
//            )
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        return http.build();
//    }
//
//
//
//}
//









package com.almubaraksuleiman.cbts.config;

import com.almubaraksuleiman.cbts.security.authentication.JwtAuthenticationFilter;
import com.almubaraksuleiman.cbts.security.authentication.manager.AuthenticationManagerImpl;
import com.almubaraksuleiman.cbts.security.authentication.providers.AdminAuthenticationProvider;
import com.almubaraksuleiman.cbts.security.authentication.providers.ExaminerAuthenticationProvider;
import com.almubaraksuleiman.cbts.security.authentication.providers.ProctorAuthenticationProvider;
import com.almubaraksuleiman.cbts.security.authentication.providers.StudentAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive enterprise security configuration for CBTS application.
 * Provides JWT authentication, CSRF protection, CORS configuration, and role-based authorization.
 * Designed for Angular frontend with Spring Boot backend architecture.
 *
 * @author Almubarak Suleiman
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminAuthenticationProvider adminAuthenticationProvider;
    private final ExaminerAuthenticationProvider examinerAuthenticationProvider;
    private final StudentAuthenticationProvider studentAuthenticationProvider;
    private final ProctorAuthenticationProvider proctorAuthenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor dependency injection for all security components.
     */
    public SecurityConfig(AdminAuthenticationProvider adminAuthenticationProvider, ExaminerAuthenticationProvider examinerAuthenticationProvider, StudentAuthenticationProvider studentAuthenticationProvider, ProctorAuthenticationProvider proctorAuthenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.adminAuthenticationProvider = adminAuthenticationProvider;
        this.examinerAuthenticationProvider = examinerAuthenticationProvider;
        this.studentAuthenticationProvider = studentAuthenticationProvider;
        this.proctorAuthenticationProvider = proctorAuthenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Creates composite authentication manager that delegates to specific providers.
     */
    @Bean
    public AuthenticationManagerImpl authenticationManager() {
        return new AuthenticationManagerImpl(List.of(adminAuthenticationProvider, examinerAuthenticationProvider, studentAuthenticationProvider, proctorAuthenticationProvider));
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for Angular frontend.
     * Allows controlled cross-origin requests from specified origins.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (replace with your Angular app URLs)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200",          // Angular dev server
                "https://your-production-domain.com" // Production domain
        ));

        // Allow essential HTTP methods
        configuration.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow necessary headers including CSRF token
        configuration.setAllowedHeaders(Arrays.asList("Authorization",       // JWT tokens
                "Content-Type",        // JSON content
                "X-Requested-With",    // AJAX requests
                "X-CSRF-TOKEN",        // CSRF protection
                "Accept"               // Content negotiation
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Set how long the CORS preflight response can be cached
        configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all endpoints

        return source;
    }

    /**
     * Main security filter chain configuration.
     * Sets up comprehensive security including CSRF, CORS, headers, and authorization.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Modern CSRF token handling for stateless APIs
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                // CORS CONFIGURATION - Enable cross-origin requests from Angular
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // CSRF PROTECTION - Defense against cross-site request forgery




                // AUTHENTICATION MECHANISMS - Disable unused authentication methods
                .httpBasic(AbstractHttpConfigurer::disable)  // Disable basic authentication
                .formLogin(AbstractHttpConfigurer::disable)    // Disable form login (using JWT instead)
                .logout(AbstractHttpConfigurer::disable)  // Disable default logout endpoint

                // AUTHORIZATION RULES - Role-based access control
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/auth/**","/ws/**"         // Authentication endpoints
                                //  "/v3/api-docs/**",        // API documentation
                               // "/swagger-ui/**",         // Swagger UI
                               // "/swagger-ui.html",       // Swagger HTML
                               // "/webjars/**",            // WebJars resource
                              //  "/swagger-resource/**",  // Swagger configuration
                               // "/actuator/health",       // Health checks (monitoring)
                              //  "/actuator/info"          // System information
                        ).permitAll()

                        // Role-based authorization for application endpoints
                        .requestMatchers("/api/admin/**")  .hasAnyRole("ADMIN", "EXAMINER", "STUDENT")             // Admin only
                        .requestMatchers("/api/examiner/**").hasAnyRole("EXAMINER", "ADMIN", "STUDENT") // Examiner + Admin
                        .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN", "EXAMINER") // Student + elevated roles
                        .requestMatchers("/api/proctor/**").hasAnyRole("PROCTOR", "ADMIN")   // Proctor + Admin
                        .requestMatchers("/api/analytics").hasAnyRole("EXAMINER", "ADMIN", "STUDENT")
                        .requestMatchers("/api/exam/**").hasAnyRole("EXAMINER", "ADMIN", "STUDENT")
                        .requestMatchers("/api/results").hasAnyRole("EXAMINER", "ADMIN", "STUDENT")

                        // All other endpoints require authentication
                        .anyRequest().authenticated())

                // SESSION MANAGEMENT - Stateless JWT authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No HTTP sessions
                )

                .authenticationManager(authenticationManager())
                // JWT AUTHENTICATION FILTER - Process JWT tokens before Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}
