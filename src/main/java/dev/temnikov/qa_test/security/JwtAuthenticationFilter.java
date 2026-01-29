package dev.temnikov.qa_test.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No Bearer -> do not enforce JWT (let other auth mechanisms handle it)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer present -> must be valid, otherwise 401
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            sendUnauthorized(response, "invalid_token", "Bearer token is missing");
            return;
        }

        try {
            String username = jwtTokenService.extractUsername(token);
            if (username == null || username.isBlank()) {
                sendUnauthorized(response, "invalid_token", "Token subject is missing");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtTokenService.isTokenValid(token, userDetails)) {
                sendUnauthorized(response, "invalid_token", "Token is invalid");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            sendUnauthorized(response, "token_expired", "Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            sendUnauthorized(response, "invalid_token", "Token is invalid");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"" + code + "\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
