package com.library.libman.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Bu importu unutmayın
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/register")
    public String register() {
        return "register-view";
    }

    @GetMapping("/login")
    public String login() {
        return "login-view";
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        // Eğer kullanıcı ADMIN yetkisine sahipse
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "admin_home";
        }

        // Kullanıcı ise, ismini sayfaya gönderiyoruz (Hata çözümü burası)
        model.addAttribute("username", authentication.getName());

        return "user_home";
    }
}