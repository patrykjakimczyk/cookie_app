package com.cookie.app.config.filter;

import com.cookie.app.config.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtValidatorFilter extends OncePerRequestFilter {
    private static final String NOT_FILTER_PATH = "/api/v1/user";
    @Value("${jwt.secret}")
    private String secret;

    @Value("${frontend.address}")
    private String frontendAddress;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = request.getHeader(JwtConstants.HEADER);

        if (null != jwt && jwt.startsWith("Bearer ")) {
            final SecretKey key = Keys.hmacShaKeyFor(this.secret.getBytes(StandardCharsets.UTF_8));
            final Claims claims;

            jwt = jwt.substring("Bearer ".length());

            try {
                claims = this.extractClaims(key, jwt);
            } catch (Exception e) {
                throw new BadCredentialsException("Invalid Token received!");
            }

            String email = claims.getSubject();
            String authorities = (String) claims.get("role");

            final Authentication auth = new UsernamePasswordAuthenticationToken(email, null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals(NOT_FILTER_PATH);
    }

    private Claims extractClaims(SecretKey key, String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
