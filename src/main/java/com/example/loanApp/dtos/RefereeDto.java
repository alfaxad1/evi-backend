package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefereeDto {
    private Integer id;
    private String name;
    private String idNumber;
    private String phoneNumber;
    private String relationship;
}
