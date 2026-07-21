package ae.gov.tdra.ssir.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for REST APIs
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Open all API endpoints temporarily for Phase 1 & 2 testing
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Permits /api/v1/registrations, GET, POST, PUT, etc.
                .anyRequest().authenticated()
            );

        return http.build();
    }
}