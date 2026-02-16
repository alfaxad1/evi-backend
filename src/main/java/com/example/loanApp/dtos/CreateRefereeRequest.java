package com.example.loanApp.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefereeRequest {
    @NotNull(message = "Name cannot be null")
    private String name;

    private String idNumber;

    @NotNull(message = "Phone cannot be null")
    private String phoneNumber;

    @NotNull(message = "Relationship cannot be null")
    private String relationship;
}
