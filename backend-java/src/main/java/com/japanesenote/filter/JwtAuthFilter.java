package com.japanesenote.filter;

import com.japanesenote.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // !!!!!!!!!!!
        // why we need to do filterChain.doFilter(request, response);
        // before every early return?

        // TODO 1: Get the "Authorization" header from the request
        //         Hint: request.getHeader("Authorization")
        String authHeader = request.getHeader("Authorization");

        // TODO 2: If the header is missing OR doesn't start with "Bearer ", skip this filter
        //         Hint: authHeader == null || !authHeader.startsWith("Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // TODO 3: Extract the raw token by removing the "Bearer " prefix (7 chars)
        String token = authHeader.substring(7);

        System.out.println("TOKEN: " + token);                                                             
        System.out.println("VALID: " + jwtUtil.validateToken(token)); 

        // TODO 4: If the token is NOT valid, skip this filter
        //         Hint: use jwtUtil.validateToken(token)
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // TODO 5: Extract the username from the token
        //         Hint: use jwtUtil.extractUsername(token)
        String username = jwtUtil.extractUsername(token);

        // Spring Security needs an Authentication object to mark the request as authenticated
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());

        // TODO 6: Put the authentication object into the SecurityContextHolder
        //         Hint: SecurityContextHolder.getContext().setAuthentication(???)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Always pass the request along to the next filter
        filterChain.doFilter(request, response);
    }
}