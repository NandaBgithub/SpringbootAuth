package com.auth.backend;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // Load user details for auth object
    @Autowired
    private CustomUserDetailsService userDetailsService;  

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/oauth2")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String accessToken = null;

        if (header != null && header.startsWith("Bearer ")) {
            accessToken = header.substring(7);
        }

        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        boolean accessTokenValid = false;
        boolean accessTokenExpired = false;

        if (accessToken != null) {
            try {
                // Validate access token (throws if invalid or expired)
                jwtService.parseClaims(accessToken, jwtService.getJwtAuthSecret());
                accessTokenValid = true;
            } catch (ExpiredJwtException e) {
                accessTokenExpired = true;
            } catch (JwtException | IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
                return;
            }
        }

        if (accessTokenValid) {
            setAuthenticationContext(accessToken, jwtService.getJwtAuthSecret());
            filterChain.doFilter(request, response);
            return;
        }

        if (accessTokenExpired && refreshToken != null) {
            if (jwtService.validateRefreshToken(refreshToken)) {
                String username = jwtService.getUsernameFromToken(refreshToken, jwtService.getJwtRefreshSecret());

                // Generate new access token
                String newAccessToken = jwtService.generateAuthenticationTokenFromUsername(username);

                // Add new access token to response header
                response.setHeader("Authorization", "Bearer " + newAccessToken);

                // Set Authentication in SecurityContext
                setAuthenticationContext(newAccessToken, jwtService.getJwtAuthSecret());

                filterChain.doFilter(request, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
                return;
            }
        }

        // No valid tokens found — continue filter chain for public endpoints or fail later
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(String token, String secret) {
        String username = jwtService.getUsernameFromToken(token, secret);

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
