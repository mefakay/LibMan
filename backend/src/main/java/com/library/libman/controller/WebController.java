package com.library.libman.controller;

import com.library.libman.entity.User;
import com.library.libman.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web sayfaları ve kimlik doğrulama işlemleri için controller.
 * Login, Register sayfalarını gösterir ve kayıt işlemlerini yönetir.
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;

    // ============================================
    // SAYFA GÖSTERİMLERİ (GET)
    // ============================================

    /**
     * Kayıt sayfasını gösterir
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * Giriş sayfasını gösterir
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Ana sayfa - Kullanıcı rolüne göre yönlendirir
     */
    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        // Admin ise admin paneline yönlendir
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "admin_home";
        }

        // Kullanıcı ise kullanıcı paneline yönlendir
        model.addAttribute("username", authentication.getName());
        return "user_home";
    }

    // ============================================
    // KAYIT İŞLEMLERİ (POST)
    // ============================================

    /**
     * Web formundan gelen kayıt isteğini işler (Thymeleaf form)
     * Form: /api/auth/register
     */
    @PostMapping("/api/auth/register")
    public String registerUserForm(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Kayıt başarıyla tamamlandı. Giriş yapabilirsiniz.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * REST API kayıt endpoint'i (JSON)
     * Endpoint: POST /api/register
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> registerUserApi(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            registeredUser.setPassword(null); // Şifreyi response'dan çıkar
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
