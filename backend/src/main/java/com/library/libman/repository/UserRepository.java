package com.library.libman.repository;

import com.library.libman.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // kullanıcı adına göre bulma
    Optional<User> findByUsername(String username);
    
    // maile göre bulma
    Optional<User> findByEmail(String email);
}














