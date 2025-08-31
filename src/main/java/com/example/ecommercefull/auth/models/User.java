package com.example.ecommercefull.auth.models;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String fullName;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;

    public User() {}
    public User(String username, String password, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() {return id;}

    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getFullName() {return fullName;}
    public Role getRole() {return role;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setFullName(String fullName) {this.fullName = fullName;}
    public void setRole(Role role) {this.role = role;}
}
