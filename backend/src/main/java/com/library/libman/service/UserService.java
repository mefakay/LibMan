package com.library.libman.service;

import com.library.libman.entity.User;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Kullanıcı işlemlerini yöneten servis
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    

    // Tüm kullanıcıları getirir
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Kullanıcı adına göre kullanıcı bulur
    public User getUserByUsername(@NonNull String username) {
        return userRepository.findByUsername(username)
                   .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
    }
    

    
    // ID'ye göre kullanıcı getirir
    public Optional<User> getUserById(@NonNull Long id) {
        return userRepository.findById(id);
    }
}





