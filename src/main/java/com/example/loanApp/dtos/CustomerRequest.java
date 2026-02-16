package com.example.loanApp.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "First name cannot be null")
    private String firstName;

    private String middleName;

    @NotNull(message = "Last name cannot be null")
    private String lastName;

    @NotNull(message = "Phone number is required")
    private String phone;

    private String address;
    private String gender;

    //@JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String residenceDetails;

    private String nationalId;

    private String county;

    @NotNull(message = "Occupation cannot be null")
    private String occupation;

    @NotNull(message = "Monthly income is required")
    private Float monthlyIncome;

    private String businessLocation;
    private String businessName;
    private Long creditScore;
}
