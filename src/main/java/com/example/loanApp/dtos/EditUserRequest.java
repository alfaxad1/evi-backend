package com.example.loanApp.dtos;

import com.example.loanApp.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EditUserRequest {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Double collectionTarget;
    private Double disbursementTarget;
    private Role role;
}
