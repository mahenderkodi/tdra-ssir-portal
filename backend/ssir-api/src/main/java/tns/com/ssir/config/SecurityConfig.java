package tns.com.ssir.config;

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
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)

			// Configure stateless session management (JWT handles the session, not the server)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
			.authorizeHttpRequests(auth -> auth
				// 1. Always allow preflight OPTIONS handshakes globally
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				// 2. Consolidated Public Whitelist [1, 3]
				.requestMatchers("/api/v1/auth/**").permitAll() // Login, OAuth, refresh tokens, register-init
				.requestMatchers(HttpMethod.POST, "/api/v1/registrations/track").permitAll() // Public track submission
				.requestMatchers("/error").permitAll()
				
				// --- ADDED SWAGGER UI WHITELISTS [1] ---
				.requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
				.requestMatchers("/swagger-ui", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				// Permit single-shot onboarding requests for pending and approved roles [3]
				.requestMatchers("/api/v1/onboarding-single/**").hasAnyRole("COMPANY_PENDING", "COMPANY_ADMIN")
				
				.requestMatchers(HttpMethod.PUT, "/api/v1/onboarding-single/**")
				.hasAnyRole("COMPANY_ADMIN", "COMPANY_USER")
				
				// ---------------------------------------

				// 3. SECURE ENDPOINTS
				// Secure Onboarding Draft Updates & Submissions (Allowed for pending users) [1, 3]
				.requestMatchers(HttpMethod.PUT, "/api/v1/registrations/draft").hasAnyRole("COMPANY_PENDING","COMPANY_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/v1/registrations/submit").hasAnyRole("COMPANY_PENDING","COMPANY_ADMIN")
				
				// Secure Status & Draft Retrievals [1, 3]				
				// Secure Status Tracking (Allows restricted pending users to view their status) [1]
				.requestMatchers(HttpMethod.GET, "/api/v1/registrations/my-status")
				.hasAnyRole("COMPANY_PENDING", "COMPANY_ADMIN", "COMPANY_USER")

				// FIX: Secure draft retrieval specifically for the applicant user (must be evaluated before wildcard)
				.requestMatchers(HttpMethod.GET, "/api/v1/registrations/my-draft")
				.hasAnyRole("COMPANY_PENDING", "COMPANY_ADMIN")

				// Rule B: Broader administrative GET endpoints evaluated SECOND [1]
				.requestMatchers(HttpMethod.GET, "/api/v1/registrations", "/api/v1/registrations/**")
				.hasAnyRole("TDRA_SUPER_ADMIN", "REVIEWER","COMPANY_ADMIN")

				// Only TDRA Admins can execute approvals/rejections (PUT status transitions)
				.requestMatchers(HttpMethod.PUT, "/api/v1/registrations/**").hasAnyRole("TDRA_SUPER_ADMIN","COMPANY_ADMIN")

				// Secure Password Setup (Requires user to be logged in with their temporary credentials) [1]
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/setup-password").hasRole("COMPANY_PENDING")
				
				// --- ADDED: Secure Sender ID endpoints ---
				.requestMatchers("/api/v1/sender-ids", "/api/v1/sender-ids/**")
				.hasAnyRole("COMPANY_ADMIN", "COMPANY_USER", "COMPANY_VIEWER", "TDRA_SUPER_ADMIN", "TDRA_APPROVER")

				.anyRequest().authenticated());

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