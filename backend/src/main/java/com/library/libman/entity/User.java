package com.library.libman.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    //User entity classının içeriği


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    public enum UserRole {
        ADMIN,      // Yönetici
        USER        // Normal kullanıcı
    }
    
  
    public User() {
    }
    
    public User(String username, String password, String email, String fullName, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
    
   
    public Long getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public UserRole getRole() {
        return role;
    }
    

    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
}
