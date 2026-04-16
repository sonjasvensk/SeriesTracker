package com.seriestracker.web;

import com.seriestracker.domain.PasswordResetToken;
import com.seriestracker.domain.PasswordResetTokenRepository;
import com.seriestracker.domain.User;
import com.seriestracker.domain.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        model.addAttribute("message", "If the email exists, reset instructions are available.");

        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(UUID.randomUUID().toString());
            token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            passwordResetTokenRepository.save(token);

            model.addAttribute("resetLink", "/reset-password?token=" + token.getToken());
        });

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, Model model) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String token,
                                      @RequestParam String password,
                                      Model model) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("passwordError", "Password must be at least 6 characters");
            return "reset-password";
        }

        User user = resetToken.getUser();
        user.setPassword(password);
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return "redirect:/login?resetSuccess";
    }
}
