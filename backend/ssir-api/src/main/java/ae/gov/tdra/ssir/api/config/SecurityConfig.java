package ae.gov.tdra.ssir.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Enable CORS directly in the security filter chain
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Disable CSRF for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Configure endpoint authorization rules
            .authorizeHttpRequests(auth -> auth
                // Explicitly permit ALL HTTP OPTIONS (preflight) handshakes globally
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Allow public access to our onboarding APIs for testing
                .requestMatchers("/api/**", "/error").permitAll() // Added "/error"
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Use setAllowedOriginPatterns with "*" instead of setAllowedOrigins.
        // This is fully compatible with allowCredentials(true) and dynamically whitelists port 4200.
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