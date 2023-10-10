package com.cookie.app.config;

import com.cookie.app.model.entity.User;
import com.cookie.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final UserRepository userRepository;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfigurer -> corsConfigurer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                    configuration.setMaxAge(3600L);
                    return configuration;
                }))
//                .csrf(csrfConfigurer -> csrfConfigurer
//                        .csrfTokenRequestHandler(requestHandler)
//                        .ignoringRequestMatchers("")
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                )
                .csrf(csrfConfigurer -> csrfConfigurer.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/register").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/configuration/ui",
                                "/swagger-resources/**",
                                "/configuration/security",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            User user = optionalUser.orElseThrow(() -> {
                throw new UsernameNotFoundException("User with given email does not exists!");
            });

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(), user.getUsername(), Collections.emptySet()
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
