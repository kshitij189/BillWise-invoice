package com.billwise.service;

import com.billwise.dto.*;
import com.billwise.model.Profile;
import com.billwise.model.User;
import com.billwise.repository.ProfileRepository;
import com.billwise.repository.UserRepository;
import com.billwise.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${react.app.url}")
    private String reactAppUrl;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("_id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());

        return new AuthResponse(result, token, null);
    }

    public AuthResponse signUp(SignUpRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords don't match");
        }

        User user = new User();
        user.setName(request.getFirstName() + " " + request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        // Create a profile for the new user
        Profile profile = new Profile();
        profile.setName(savedUser.getName());
        profile.setEmail(savedUser.getEmail());
        profile.setUserId(new ArrayList<>(List.of(savedUser.getId())));
        Profile savedProfile = profileRepository.save(profile);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("_id", savedUser.getId());
        result.put("name", savedUser.getName());
        result.put("email", savedUser.getEmail());

        return new AuthResponse(result, token, savedProfile);
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with this email does not exist"));

        // Generate random 32-byte hex token
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        String token = sb.toString();

        user.setResetToken(token);
        user.setExpireToken(new Date(System.currentTimeMillis() + 3600000)); // 1 hour
        userRepository.save(user);

        // Send reset email
        String resetLink = reactAppUrl + "/reset/" + token;
        String subject = "BillWise - Password Reset";
        String htmlContent = buildResetEmailHtml(user.getName(), resetLink);

        try {
            emailService.sendHtmlEmail("noreply@billwise.com", user.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            // Log but don't fail - token is saved
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Check your email for the reset link");
        return response;
    }

    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (user.getExpireToken() == null || user.getExpireToken().before(new Date())) {
            throw new RuntimeException("Token has expired. Try again.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetToken(null);
        user.setExpireToken(null);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password successfully updated");
        return response;
    }

    private String buildResetEmailHtml(String name, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: auto; background: white; border-radius: 8px; padding: 40px; }
                    .header { text-align: center; color: #1976d2; }
                    .btn { display: inline-block; background-color: #1976d2; color: white; padding: 14px 28px;
                           text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1 class="header">BillWise</h1>
                    <p>Hi %s,</p>
                    <p>You requested a password reset. Click the button below to set a new password:</p>
                    <p style="text-align: center;">
                        <a href="%s" class="btn">Reset Password</a>
                    </p>
                    <p>This link will expire in 1 hour.</p>
                    <p>If you did not request this, please ignore this email.</p>
                    <div class="footer">
                        <p>&copy; BillWise. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, resetLink);
    }
}
