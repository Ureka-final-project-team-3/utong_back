package com.ureka.team3.utong_backend.auth.filter;


import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ureka.team3.utong_backend.auth.service.RedisTokenService;
import com.ureka.team3.utong_backend.auth.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTokenService redisTokenService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, 
                                  UserDetailsService userDetailsService,
                                  RedisTokenService redisTokenService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.redisTokenService = redisTokenService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String email = null;
        String jwtToken = null;
        
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            if (redisTokenService.isTokenBlacklisted(jwtToken)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            try {
                email = jwtUtil.extractEmail(jwtToken);
            } catch (Exception e) {
                logger.warn("JWT Token has expired or is invalid");
            }
        }
        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            if (jwtUtil.validateToken(jwtToken, jwtUtil.extractAccountId(jwtToken))) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}