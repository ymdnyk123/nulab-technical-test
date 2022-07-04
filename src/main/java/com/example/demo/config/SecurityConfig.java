package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * セキュリティ設定
 */
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
			.authorizeHttpRequests(
					auth -> auth
					.mvcMatchers("/webjars/**").permitAll()
					.anyRequest().authenticated())
			.oauth2Login(
					login -> login
					.loginPage("/login").permitAll())
			.logout(
					logout -> logout
					.permitAll())
			.csrf(
					csrf -> csrf
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
		
		return http.build();
	}
}
