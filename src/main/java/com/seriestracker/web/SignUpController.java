package com.seriestracker.web;

import com.seriestracker.domain.User;
import com.seriestracker.domain.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignUpController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignUpController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String showSignUp(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("user") User user,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return "signup";
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
            return "signup";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }
}
