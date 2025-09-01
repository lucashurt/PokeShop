package com.example.ecommercefull.auth;

import com.example.ecommercefull.auth.DTOs.AuthRequest;
import com.example.ecommercefull.auth.DTOs.AuthResponse;
import com.example.ecommercefull.auth.DTOs.RegisterRequest;
import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.Role;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.RoleRepository;
import com.example.ecommercefull.auth.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        Role role = roleRepository.findByName(registerRequest.role()).orElse(null);
        if(role == null) {
            return new AuthResponse("role does not exists");
        }
        User user = new User();

        if(role.getName().equals("BUSINESS")){
            user = new Business();
        }

        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setFullName(registerRequest.fullName());
        user.setRole(role);
        userRepository.save(user);
        return new AuthResponse("success");
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
        return new AuthResponse("success");
    }

}
