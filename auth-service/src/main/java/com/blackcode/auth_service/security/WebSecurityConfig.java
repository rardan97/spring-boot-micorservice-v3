package com.blackcode.auth_service.security;

import com.blackcode.auth_service.security.jwt.AuthEntryPointJwt;
import com.blackcode.auth_service.security.jwt.AuthTokenFilter;
import com.blackcode.auth_service.security.service.UserAuthDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;

    private final AuthEntryPointJwt unauthorizedHandler;

    private final AuthTokenFilter authenticationJwtTokenFilter;

    public WebSecurityConfig(UserAuthDetailsServiceImpl userAuthDetailsServiceImpl, AuthEntryPointJwt unauthorizedHandler, AuthTokenFilter authenticationJwtTokenFilter) {
        this.userAuthDetailsServiceImpl = userAuthDetailsServiceImpl;
        this.unauthorizedHandler = unauthorizedHandler;
        this.authenticationJwtTokenFilter = authenticationJwtTokenFilter;
    }


    @Bean
    public DaoAuthenticationProvider userAuthDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userAuthDetailsServiceImpl);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(userAuthDaoAuthenticationProvider()));
//        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/public/**").permitAll()
//                        .requestMatchers("/api/petugas/**").hasAnyRole("ADMIN", "PETUGAS")
                        .anyRequest().authenticated());
        http.cors(customizer -> customizer.configurationSource(corsConfigurationSource()));
//        http.authenticationProvider(userDaoAuthenticationProvider());
//        http.authenticationProvider(staffDaoAuthenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
