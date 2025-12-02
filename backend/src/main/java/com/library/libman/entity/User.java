package com.library.libman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.lang.NonNull;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter arası olmalı")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", 
             message = "Kullanıcı adı sadece harf, rakam ve alt çizgi içerebilir")
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalı")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "Email adresi boş olamaz")
    @Pattern(regexp = ".*@.*", message = "Email adresi @ işareti içermelidir")
    @Column(nullable = false)
    private String email;
    
    @NotBlank(message = "Ad soyad boş olamaz")
    @Size(min = 2, max = 100, message = "Ad soyad 2-100 karakter arası olmalı")
    @Column(nullable = false)
    private String fullName;
    
    @NotNull(message = "Kullanıcı rolü belirtilmelidir")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    public enum UserRole {
        ADMIN,
        USER
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
    
   
    @NonNull
    public Long getId() {
        return id != null ? id : 0L;
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
