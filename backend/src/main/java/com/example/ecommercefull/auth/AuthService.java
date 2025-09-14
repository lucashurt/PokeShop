package com.example.ecommercefull.auth;

import com.example.ecommercefull.auth.DTOs.AuthRequest;
import com.example.ecommercefull.auth.DTOs.AuthResponse;
import com.example.ecommercefull.auth.DTOs.RegisterRequest;
import com.example.ecommercefull.auth.util.JwtUtil;
import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.RoleEnum;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,PasswordEncoder passwordEncoder,JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.findByUsername(registerRequest.username()).isPresent()) {
            return new AuthResponse("username already exists");
        }
        RoleEnum userRole;

        if ("BUSINESS".equalsIgnoreCase(registerRequest.role())) {
            userRole = RoleEnum.ROLE_BUSINESS;
        } else if ("CUSTOMER".equalsIgnoreCase(registerRequest.role())) {
            userRole = RoleEnum.ROLE_CUSTOMER;
        } else {
            return new AuthResponse("role does not exist");
        }

        User user;
        if(userRole == RoleEnum.ROLE_BUSINESS){
            user = new Business();
        }
        else{
            user = new User();
        }
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setFullName(registerRequest.fullName());
        user.setRole(userRole);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(),user.getRole().name(),user.getId());
        return new AuthResponse("success",token,user.getRole().name(),user.getId());
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(authRequest.username());
        if(optionalUser.isEmpty()) {
            return new AuthResponse("invalid credentials");
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            return new AuthResponse("invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());

        return new AuthResponse("success",token,user.getRole().name(),user.getId());
    }

}
