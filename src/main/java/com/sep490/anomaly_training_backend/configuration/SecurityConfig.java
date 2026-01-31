package com.sep490.anomaly_training_backend.configuration;

import com.sep490.anomaly_training_backend.security.JwtAuthenticationFilter;
import com.sep490.anomaly_training_backend.security.OAuth2AuthenticationFailureHandler;
import com.sep490.anomaly_training_backend.security.OAuth2AuthenticationSuccessHandler;
import com.sep490.anomaly_training_backend.service.impl.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config. annotation.web.builders.HttpSecurity;
import org.springframework. security.config.annotation.web. configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org. springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto. bcrypt.BCryptPasswordEncoder;
import org.springframework.security. crypto.password.PasswordEncoder;
import org.springframework.security. web.SecurityFilterChain;
import org.springframework.security.web. authentication.UsernamePasswordAuthenticationFilter;
import org.springframework. web.cors.CorsConfiguration;
import org.springframework.web. cors.CorsConfigurationSource;
import org.springframework.web. cors.UrlBasedCorsConfigurationSource;

import java. util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/api/v1/training-plans/**").hasRole("TEAM_LEADER")
                        .requestMatchers("/api/v1/training-result/**").hasAnyRole("TEAM_LEADER", "FINAL_INSPECTION")
                        .requestMatchers("/api/v1/training-topics/**").hasAnyRole("TEAM_LEADER", "SUPERVISOR")
                        .requestMatchers("/api/v1/defects/**").hasAnyRole("TEAM_LEADER", "SUPERVISOR")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(List.of(allowedHeaders));
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders. split(",")));
        configuration. setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}