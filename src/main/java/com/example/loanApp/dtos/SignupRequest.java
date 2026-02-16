package com.example.loanApp.dtos;

import com.example.loanApp.enums.Role;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Role role;
    private Double monthlyDisbursementTarget;
    private Double monthlyCollectionTarget;
    private String firstName;
    private String lastName;
}
