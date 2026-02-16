package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    private Integer userId;
    private String name;
    private String email;
    private String role;
}
