package com.alana.model;

import com.google.firebase.Timestamp;

public class User {
    private String email;
    private String role;
    private Timestamp criadoEm;

    public User() {} // Construtor vazio para Firestore

    public User(String email, String role, Timestamp criadoEm) {
        this.email = email;
        this.role = role;
        this.criadoEm = criadoEm;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Timestamp getCriadoEm() { return criadoEm; }
}
