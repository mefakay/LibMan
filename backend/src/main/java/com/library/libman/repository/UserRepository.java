package com.library.libman.repository;

import com.library.libman.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//Database işlemleri için repository classı

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Kullanıcı adına göre bulma
    Optional<User> findByUsername(String username);
    
    // Email'e göre bulma
    Optional<User> findByEmail(String email);
}














