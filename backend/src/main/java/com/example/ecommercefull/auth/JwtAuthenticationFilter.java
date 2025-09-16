package com.example.ecommercefull.auth;

import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.auth.util.JwtUtil;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String authHeader = request.getHeader("Authorization");
        System.out.println("JwtAuthenticationFilter - Authorization header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JwtAuthenticationFilter - No Authorization header or wrong format, skipping filter.");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        System.out.println("JwtAuthenticationFilter - Extracted JWT: " + jwt);

        try {
            if(jwtUtil.isValid(jwt)){
                System.out.println("JwtAuthenticationFilter - JWT is valid");
                String username = jwtUtil.getUsernameFromToken(jwt);
                String role = jwtUtil.getRole(jwt);
                System.out.println("JwtAuthenticationFilter - Username from token: " + username);
                System.out.println("JwtAuthenticationFilter - Role from token: " + role);

                Optional<User> user = userRepository.findByUsername(username);
                if(user.isPresent()){
                    System.out.println("JwtAuthenticationFilter - User found in DB: " + username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user.get().getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JwtAuthenticationFilter - SecurityContext updated with authenticated user");
                } else {
                    System.out.println("JwtAuthenticationFilter - User not found in DB: " + username);
                }
            } else {
                System.out.println("JwtAuthenticationFilter - JWT is invalid");
            }
        } catch (Exception e) {
            System.out.println("JwtAuthenticationFilter - Exception during JWT validation: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
