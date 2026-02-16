package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private LocalDateTime userSince;
    private boolean active;
    private Float collectionTarget;
    private Float disbursementTarget;
}
