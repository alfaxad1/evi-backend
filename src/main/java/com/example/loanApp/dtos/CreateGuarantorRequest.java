package com.example.loanApp.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuarantorRequest {
    @NotNull(message = "Name cannot be null")
    private String name;

    private String nationalId;

    @NotNull(message = "Phone cannot be null")
    private String phoneNumber;

    private String passPhoto;

    @NotNull(message = "Relationship cannot be null")
    private String relationship;

    private String businessLocation;
    private String residenceDetails;

}
