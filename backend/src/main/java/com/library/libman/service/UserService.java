package com.library.libman.service;

import com.library.libman.entity.User;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.BorrowRequestRepository;
import com.library.libman.repository.ProfileUpdateRequestRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private ProfileUpdateRequestRepository profileUpdateRequestRepository; // ✅ YENİ

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor: " + user.getUsername());
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(User.UserRole.USER);
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // KULLANICI SİLME (İlişkili verilerle beraber)
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 1. Kullanıcının profil güncelleme isteklerini sil (✅ YENİ)
        var profileRequests = profileUpdateRequestRepository.findAll()
                .stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .toList();
        profileUpdateRequestRepository.deleteAll(profileRequests);

        // 2. Kullanıcının ödünç isteklerini sil
        var requests = borrowRequestRepository.findByUser(user);
        borrowRequestRepository.deleteAll(requests);

        // 3. Kullanıcının ödünç kayıtlarını sil
        var borrows = borrowRepository.findByUser(user);
        borrowRepository.deleteAll(borrows);

        // 4. Kullanıcıyı sil
        userRepository.delete(user);
    }
}