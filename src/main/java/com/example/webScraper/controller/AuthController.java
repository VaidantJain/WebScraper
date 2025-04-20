package com.example.webScraper.controller;

import com.example.webScraper.model.User;
import com.example.webScraper.service.UserService;
import com.example.webScraper.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,@RequestParam(value = "logout", required = false) String logout, Model model) {
        System.out.println("in get login");
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out.");
        }
        return "login";
    }

//    @PostMapping("/login")
//    public String loginUser(@ModelAttribute("user") User user, Model model) {
//        Optional<User> existingUser = userService.findByUsername(user.getUsername());
//        System.out.println("password: "+user.getPassword());
//        System.out.println("username: "+user.getUsername());
//
//        if (existingUser.isPresent() && passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
//            return "redirect:/dashboard";
//        } else {
//            model.addAttribute("errorMessage", "Invalid username or password.");
//            return "redirect:/login?error";
//        }
//    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        System.out.println("in get register");
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model, RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("usernameError", "Username is already taken");
            return "register"; // return to the same form view
        }

        // Check if email exists
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("emailError", "Email is already registered");
            return "register";
        }

        user.setRoles(Collections.singletonList("ROLE_USER"));
        user.setCreatedAt(new Date());
        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("message", "Account created successfully! Please log in.");
        return "redirect:/login";
    }
}
