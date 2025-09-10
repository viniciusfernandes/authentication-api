package br.com.ovigia.config;

import br.com.ovigia.service.IJwtService;
import br.com.ovigia.service.IUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final IJwtService jwtService;
	private final IUserService userService;
	private final PasswordEncoder passwordEncoder;
	
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService((UserDetailsService) userService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtService, userService);
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	// Virtual threads executor for Spring Security chain and MVC
	@Bean
	public Executor applicationTaskExecutor() {
		return java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**").permitAll()
				.requestMatchers("/oauth2/**").permitAll()
				.requestMatchers("/login/oauth2/**").permitAll()
				.requestMatchers("/actuator/**").permitAll()
				.anyRequest().authenticated()
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authenticationProvider(authenticationProvider())
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	@RequiredArgsConstructor
	public static class JwtAuthenticationFilter extends OncePerRequestFilter {
		private final IJwtService jwtService;
		private final IUserService userService;
		
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
								  FilterChain filterChain) throws ServletException, IOException {
		final String requestURI = request.getRequestURI();
		
		// Skip JWT processing for registration and other public endpoints
		if (requestURI.equals("/api/auth/register") || 
			requestURI.equals("/api/auth/verify-email") ||
			requestURI.equals("/api/auth/forgot-password") ||
			requestURI.equals("/api/auth/reset-password") ||
			requestURI.startsWith("/oauth2/") ||
			requestURI.startsWith("/login/oauth2/")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String userEmail;
		
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
			
			jwt = authHeader.substring(7);
			userEmail = jwtService.extractUsername(jwt);
			
			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				var userDetails = userService.loadUserByUsername(userEmail);
				
				if (jwtService.validateToken(jwt, userDetails)) {
					var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
					authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
			
			filterChain.doFilter(request, response);
		}
	}
}
