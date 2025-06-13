package com.auth.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class Endpoints {

    // For db
    @Autowired
    JwtRepository jwtRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService = new JwtService();

    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth(){
        return ResponseEntity.ok("Authenticated");
    }

    @GetMapping("/user-info")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal){
        return principal.getAttributes();
    }

    @PostMapping("/CustomLogin")
    public ResponseEntity<Map<String, String>> customlogin(@RequestBody Map<String, String> loginRequest, HttpServletResponse response) {
        String username = loginRequest.get("username");
        String rawPassword = loginRequest.get("password");

        Optional<User> userOpt = jwtRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        // Generate JWT tokens
        String accessToken = jwtService.generateAuthenticationTokenFromUsername(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        // Set refresh token as HttpOnly cookie for seven days
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);           
        refreshCookie.setPath("/");              
        refreshCookie.setMaxAge((int)(7 * 24 * 60 * 60)); 
        response.addCookie(refreshCookie);

        // Return access token in response body json format 
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("tokenType", "Bearer");

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/CustomSignup")
    public ResponseEntity<Map<String, String>> customSignup(@RequestBody Map<String, String> signupRequest,
                                                            HttpServletResponse response) {
        String username = signupRequest.get("username");
        String email = signupRequest.get("email");
        String rawPassword = signupRequest.get("password");

        if (jwtRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken"));
        }

        if (jwtRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);

        jwtRepository.save(newUser);

        // Generate JWT tokens after successful signup
        String accessToken = jwtService.generateAuthenticationTokenFromUsername(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        // Sets refresh token as HttpOnly cookie for 7 days
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);           
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int)(7 * 24 * 60 * 60));
        response.addCookie(refreshCookie);

        // Return access token in response body
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("tokenType", "Bearer");

        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }
}
