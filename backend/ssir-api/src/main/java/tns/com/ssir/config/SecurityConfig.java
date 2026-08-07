package tns.com.ssir.config;

//import tns.com.ssir.api.security.CustomAuthenticationEntryPoint; // Imported
//import tns.com.ssir.api.security.CustomAccessDeniedHandler;         // Imported
import java.util.List;

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables fine-grained method security: @PreAuthorize("hasRole('...')")
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	// @Autowired
	// private CustomAuthenticationEntryPoint authenticationEntryPoint; // Injected
	// [1]

	// @Autowired
	// private CustomAccessDeniedHandler accessDeniedHandler; // Injected [1]

	@Value("${cors.allowed-origins:http://localhost:4200}")
	private List<String> allowedOrigins;

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12); // Hardened to 12 rounds of hashing for production
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
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)

				// Configure stateless session management
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// Register exception handlers to return our unified ErrorResponse DTO globally
				// [1, 2]
//            .exceptionHandling(exception -> exception
//                .authenticationEntryPoint(authenticationEntryPoint)
//                .accessDeniedHandler(accessDeniedHandler)
//            )
//            
				.authorizeHttpRequests(auth -> auth
						// 1. Always allow preflight OPTIONS handshakes globally
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// 2. Merged Public Whitelist [1, 3]
						.requestMatchers("/api/v1/auth/**").permitAll() // Login, OAuth, refresh tokens
						.requestMatchers(HttpMethod.POST, "/api/v1/registrations", "/api/v1/registrations/track")
						.permitAll() // Onboarding & Tracking submissions whitelisted [3]
						.requestMatchers("/error").permitAll()
						
						.requestMatchers(HttpMethod.PUT, "/api/v1/registrations/draft").hasRole("COMPANY_PENDING")
		                .requestMatchers(HttpMethod.POST, "/api/v1/registrations/submit").hasRole("COMPANY_PENDING")
		                
						.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register-init").permitAll() // Whitelisted [1]
		                .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
		                .requestMatchers("/error").permitAll()
						
						
						// --- ADDED SWAGGER UI WHITELISTS [1] ---
		                .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
		                .requestMatchers("/swagger-ui", "/swagger-ui/**", "/swagger-ui.html").permitAll()
		                // ---------------------------------------
		                

						// 3. SECURE ENDPOINTS
						// Rule A: Specific tracking endpoint must be evaluated FIRST [1]
						.requestMatchers(HttpMethod.GET, "/api/v1/registrations/my-status")
						.hasAnyRole("COMPANY_PENDING", "COMPANY_ADMIN", "COMPANY_USER")

						// Rule B: Broader administrative GET endpoints evaluated SECOND [1]
						.requestMatchers(HttpMethod.GET, "/api/v1/registrations", "/api/v1/registrations/**")
						.hasAnyRole("TDRA_SUPER_ADMIN", "REVIEWER")

						// Only TDRA Admins can execute approvals/rejections (PUT status transitions)
						.requestMatchers(HttpMethod.PUT, "/api/v1/registrations/**").hasRole("TDRA_SUPER_ADMIN")

						.requestMatchers(HttpMethod.POST, "/api/v1/auth/setup-password").hasRole("COMPANY_PENDING")

						.anyRequest().authenticated());

		// Inject the database authentication provider
		http.authenticationProvider(authenticationProvider());

		// Inject our custom JWT Verification filter before Spring's
		// UsernamePasswordAuthenticationFilter
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