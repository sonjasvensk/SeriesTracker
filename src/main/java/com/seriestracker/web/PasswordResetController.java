package com.seriestracker.web;

import com.seriestracker.domain.PasswordResetToken;
import com.seriestracker.domain.PasswordResetTokenRepository;
import com.seriestracker.domain.User;
import com.seriestracker.domain.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordResetController.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetTokenRepository passwordResetTokenRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute("invalidToken")
    public Boolean invalidTokenDefault() {
        return Boolean.FALSE;
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, Model model) {
        model.addAttribute("message", "If the email exists, reset instructions are available.");

        try {
            userRepository.findByEmail(email).ifPresent(user -> {
                PasswordResetToken token = new PasswordResetToken();
                token.setUser(user);
                token.setToken(UUID.randomUUID().toString());
                token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
                passwordResetTokenRepository.save(token);

                model.addAttribute("resetToken", token.getToken());
                model.addAttribute("resetLink", "/reset-password?token=" + token.getToken());
                model.addAttribute("deliveryNotice", "Email sending is not configured yet. Use the reset link below.");
                LOG.info("Password reset link for {}: /reset-password?token={}", email, token.getToken());
            });
        } catch (Exception ex) {
            LOG.error("Forgot password flow failed for email {}", email, ex);
            model.addAttribute("message", "Something went wrong. Please try again.");
        }

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }

        model.addAttribute("invalidToken", false);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String token,
                                      @RequestParam String password,
                                      Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("invalidToken", false);
            model.addAttribute("token", token);
            model.addAttribute("passwordError", "Password must be at least 6 characters");
            return "reset-password";
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return "redirect:/login?resetSuccess";
    }
}
