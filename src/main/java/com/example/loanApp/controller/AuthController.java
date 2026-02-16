package com.example.loanApp.controller;

import com.example.loanApp.SecurityConfig.JwtUtils;
import com.example.loanApp.dtos.ApiResponse;
import com.example.loanApp.dtos.LoginRequest;
import com.example.loanApp.dtos.LoginResponse;
import com.example.loanApp.entities.User;
import com.example.loanApp.repository.UserRepository;
import com.example.loanApp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtils jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.security.cookie-secure}")
    private boolean isSecure;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(7 * 24 * 60 * 60)
                .build();

//        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
//                .success(true)
//                .message("Login successful")
//                .data(LoginResponse.builder()
//                        .accessToken(response.getAccessToken())
//                        .expirationTime(response.getExpirationTime())
//                        .role(response.getRole())
//                        .userId(response.getUserId())
//                        .tokenType("Bearer")
//                        .build())
//                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired refresh token"));
        }

        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.createAccessToken(user);
        Long expirationTime = jwtUtil.extractExpiration(newAccessToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "expirationTime", expirationTime
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // We create a cookie with the same name, but maxAge = 0
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")     // Must match the path used when creating the cookie
                .maxAge(0)     // This tells the browser to delete it immediately
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/tester")
    public ResponseEntity<String> tester(){
        return ResponseEntity.ok("Loan App running....");
    }

}

