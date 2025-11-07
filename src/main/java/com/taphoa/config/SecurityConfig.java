package com.taphoa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
   @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/register", "/login", "/products/**", "/search", 
                           "/cart", "/cart/**", "/order/**", "/logout",  // ← THÊM /order/**
                           "/css/**", "/js/**", "/images/**").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin().disable()
        .logout().disable();
    
    return http.build();
}
}