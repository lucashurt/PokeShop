package com.example.ecommercefull.auth;

import com.example.ecommercefull.auth.DTO.AuthRequest;
import com.example.ecommercefull.auth.DTO.AuthResponse;
import com.example.ecommercefull.auth.DTO.RegisterRequest;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.RoleRepository;
import com.example.ecommercefull.auth.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.findByUsername(registerRequest.username()).isPresent()) {
            return new AuthResponse("username already exists");
        }
        if(roleRepository.findByName(registerRequest.role()).isEmpty()) {
            return new AuthResponse("role does not exists");
        }
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setFullName(registerRequest.fullName());
        userRepository.save(user);
        return new AuthResponse("success");
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        User user = userRepository.findByUsername(authRequest.username()).orElseThrow();
        if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            return new AuthResponse("invalid credentials");
        }
        return new AuthResponse("success");
    }

}
