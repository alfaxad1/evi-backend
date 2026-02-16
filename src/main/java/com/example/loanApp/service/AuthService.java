package com.example.loanApp.service;

import com.example.loanApp.SecurityConfig.JwtUtils;
import com.example.loanApp.dtos.LoginResponse;
import com.example.loanApp.dtos.SignupRequest;
import com.example.loanApp.dtos.UserData;
import com.example.loanApp.entities.User;
import com.example.loanApp.exceptions.ApplicationException;
import com.example.loanApp.exceptions.AuthenticationException;
import com.example.loanApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtil;
    private final AuthenticationManager authenticationManager;

    public User signup(SignupRequest request) {
        Optional<User> users = userRepository.findByEmail(request.getEmail());
        if (users.isPresent()) {
            throw new ApplicationException("User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setActive(true);
        user.setRole(request.getRole());
        user.setMonthlyCollectionTarget(request.getMonthlyCollectionTarget());
        user.setMonthlyDisbursementTarget(request.getMonthlyDisbursementTarget());
        return userRepository.save(user);
    }

    public LoginResponse login(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            User user = (User) authentication.getPrincipal();

            String token = jwtUtil.createAccessToken(user);
            String refreshToken = jwtUtil.createRefreshToken(user);
            Long accessTokenExpiration = jwtUtil.extractExpiration(token);

            return LoginResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .expirationTime(accessTokenExpiration)
                    .userData(UserData.builder()
                            .userId(jwtUtil.extractUserId(token))
                            .name(jwtUtil.extractName(token))
                            .email(email)
                            .role(jwtUtil.extractRole(token))
                            .build()
                    )
                    .tokenType("Bearer")
                    .build();

        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid email or password");
        }
        catch (Exception e) {
            throw new ApplicationException("Error logging in:  " + e.getMessage());
        }
    }
}