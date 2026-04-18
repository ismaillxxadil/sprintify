package com.sprintify.identityservice.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sprintify.identityservice.entity.User;
import com.sprintify.identityservice.repository.UserRepository;

@Component
//OncePerRequestFilter ensures that the filter is executed once per request.
//work like middelware.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            if (email == null || !jwtService.isTokenValid(token, email)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            //if the user is not authenticated yet, we will authenticate them based on the token
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(email)
                        .filter(foundUser -> foundUser.getRole() != null)
                        .orElse(null);

                if (user == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }

                // Check if user is banned
                if (user.isBanned()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is banned");
                    return;
                }

                // create an authentication token with the user's email and role, and set it in the security context
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,// no credentials since we're using JWT
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
                // set the details of the authentication token and store it in the security context
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // store the authentication token in the security context
                //  so that it can be accessed by other parts of the application
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        }
    }
}