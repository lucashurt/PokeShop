package com.example.ecommercefull.auth.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    public Role() {}
    public Role(String name) {
        this.name = name;
    }

    public String getName() {return name;}
    public void setRole(String role) {}
}
