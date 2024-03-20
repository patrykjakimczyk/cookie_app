package com.cookie.app.config;

import com.cookie.app.config.filter.CsrfCookieFilter;
import com.cookie.app.config.filter.JwtGeneratorFilter;
import com.cookie.app.config.filter.JwtValidatorFilter;
import com.cookie.app.model.entity.User;
import com.cookie.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.CorsConfiguration;

import java.util.*;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String ROLE_PREFIX = "ROLE_";
    private final UserRepository userRepository;
    @Value("${frontend.address}")
    private String frontendAddress;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
        delegate.setCsrfRequestAttributeName(null);
        CsrfTokenRequestHandler requestHandler = delegate::handle;

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfigurer -> corsConfigurer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of(this.frontendAddress));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                    configuration.setMaxAge(3600L);
                    return configuration;
                }))
                .csrf(csrfConfigurer -> csrfConfigurer
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // used as a workaround for testing endpoints in swagger
                        .ignoringRequestMatchers(
                                "/api/v1/register",
                                "/api/v1/pantry",
                                "/api/v1/pantry/**",
                                "/api/v1/product",
                                "/api/v1/group",
                                "/api/v1/group/**",
                                "/api/v1/shopping-lists",
                                "/api/v1/shopping-lists/**",
                                "/api/v1/recipes",
                                "/api/v1/recipes/**",
                                "/api/v1/meals",
                                "/api/v1/meals/**"
                        )
//                                .ignoringRequestMatchers("/api/v1/register")
                        .csrfTokenRequestHandler(requestHandler)
                )
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JwtValidatorFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JwtGeneratorFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/v1/register", "/api/v1/recipes/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/configuration/ui",
                                "/swagger-resources/**",
                                "/configuration/security",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            User user = optionalUser.orElseThrow(() ->
                    new UsernameNotFoundException("User with given email does not exists!"));

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(), user.getPassword(), authorities
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
