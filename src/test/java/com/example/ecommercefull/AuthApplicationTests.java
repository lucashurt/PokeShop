package com.example.ecommercefull;

import com.example.ecommercefull.auth.DTOs.AuthRequest;
import com.example.ecommercefull.auth.DTOs.AuthResponse;
import com.example.ecommercefull.auth.DTOs.RegisterRequest;
import com.example.ecommercefull.auth.models.Role;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.RoleRepository;
import com.example.ecommercefull.auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role("CUSTOMER");
        roleRepository.save(role);

        Role role2 = new Role("BUSINESS");
        roleRepository.save(role2);

        User user = new User("yuly",passwordEncoder.encode("password"),"yulissa morejon",role);
        userRepository.save(user);

        User user2 = new User("marlon",passwordEncoder.encode("password"),"marlon pavoni",role2);
        userRepository.save(user2);
    }

    @Test
    @DirtiesContext
    void shouldCreateUser() {
        RegisterRequest registerRequest = new RegisterRequest("lucas","password","lucas hurtado","CUSTOMER");
        AuthResponse authResponse = restTemplate.postForObject("/auth/register", registerRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("success");
        User user = userRepository.findByUsername("lucas").get();
        assertThat(user.getUsername()).isEqualTo("lucas");
        assertThat(user.getFullName()).isEqualTo("lucas hurtado");
        assertThat(user.getRole().getName()).isEqualTo("CUSTOMER");
    }

    @Test
    void shouldCreateBusinessUser() {
        RegisterRequest registerRequest = new RegisterRequest("lucas","password","lucas hurtado","BUSINESS");
        AuthResponse authResponse = restTemplate.postForObject("/auth/register", registerRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("success");
        User user = userRepository.findByUsername("lucas").get();
        assertThat(user.getUsername()).isEqualTo("lucas");
        assertThat(user.getFullName()).isEqualTo("lucas hurtado");
        assertThat(user.getRole().getName()).isEqualTo("BUSINESS");
    }

    @Test
    void shouldNotCreateUserWithTakenUsername(){
        RegisterRequest registerRequest = new RegisterRequest("yuly","password","yulissa morejon","CUSTOMER");
        AuthResponse authResponse = restTemplate.postForObject("/auth/register", registerRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("username already exists");
    }

    @Test
    void shouldNotCreateUserWithIncorrectRole(){
        RegisterRequest registerRequest = new RegisterRequest("lucas","password","yulissa morejon","BAD_ROLE");
        AuthResponse authResponse = restTemplate.postForObject("/auth/register", registerRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("role does not exists");
    }

    @Test
    void shouldReturnAuthenticatedUser(){
        AuthRequest authRequest = new AuthRequest("yuly","password");
        AuthResponse authResponse = restTemplate
                .postForObject("/auth/login", authRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("success");
    }

    @Test
    void shouldAuthenticateBusinessUser(){
        AuthRequest authRequest = new AuthRequest("marlon","password");
        AuthResponse authResponse = restTemplate
                .postForObject("/auth/login", authRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("success");
    }

    @Test
    void shouldNotReturnIncorrectPassword(){
        AuthRequest authRequest = new AuthRequest("yuly","wrongPassword");
        AuthResponse authResponse = restTemplate
                .postForObject("/auth/login", authRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("invalid credentials");
    }
    @Test
    void shouldNotReturnIncorrectUsername() {
        AuthRequest authRequest = new AuthRequest("yuky", "password");
        AuthResponse authResponse = restTemplate
                .postForObject("/auth/login", authRequest, AuthResponse.class);
        assertThat(authResponse.message()).isEqualTo("invalid credentials");
    }
    }
