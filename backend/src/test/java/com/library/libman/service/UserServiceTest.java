package com.library.libman.service;

import com.library.libman.entity.User;
import com.library.libman.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnListFromRepository() {
        // GIVEN
        User u1 = new User();
        u1.setId(1L);
        u1.setUsername("user1");

        User u2 = new User();
        u2.setId(2L);
        u2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        // WHEN
        List<User> result = userService.getAllUsers();

        // THEN
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserByUsername_shouldReturnUser_whenExists() {
        // GIVEN
        String username = "kaan";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // WHEN
        User result = userService.getUserByUsername(username);

        // THEN
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getUserByUsername_shouldThrow_whenUserNotFound() {
        // GIVEN
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.getUserByUsername(username));

        assertTrue(ex.getMessage().contains("Kullanıcı bulunamadı"));
        verify(userRepository, times(1)).findByUsername(username);
    }
}