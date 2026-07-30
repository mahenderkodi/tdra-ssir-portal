package tns.com.ssir.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tns.com.ssir.security.CustomUserDetailsService;
import tns.com.ssir.security.JwtAuthenticationFilter;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure stateless session management (JWT handles the session, not the server)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // 1. Always allow preflight OPTIONS handshakes globally
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 2. Public whitelists
                .requestMatchers("/api/v1/auth/**").permitAll() // Login, OAuth, refresh tokens
                .requestMatchers(HttpMethod.POST, "/api/v1/registrations").permitAll() // Public registration submit
                .requestMatchers("/error").permitAll()
                
                // 3. SECURE ENDPOINTS
                // Only TDRA Staff (Admins and Reviewers) can fetch registration lists or specific request details
                // Updated to explicitly include the base path "/api/v1/registrations" alongside the wildcard
                .requestMatchers(HttpMethod.GET, "/api/v1/registrations", "/api/v1/registrations/**")
                .hasAnyRole("TDRA_SUPER_ADMIN", "REVIEWER")
                
                // Only TDRA Admins can execute approvals/rejections (PUT status transitions)
                .requestMatchers(HttpMethod.PUT, "/api/v1/registrations/**").hasRole("TDRA_SUPER_ADMIN")
                
             // 2. Public whitelists
                .requestMatchers("/api/v1/auth/**").permitAll() // Login, OAuth, refresh tokens
                .requestMatchers(HttpMethod.POST, "/api/v1/registrations", "/api/v1/registrations/track").permitAll()
                .requestMatchers("/error").permitAll()
                
                .anyRequest().authenticated()
            );

        // Inject the database authentication provider
        http.authenticationProvider(authenticationProvider());
        
        // Inject our custom JWT Verification filter before Spring's UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); 
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}