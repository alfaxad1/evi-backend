package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailsDto {
    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String nationalId;
    private String nationalIdPhoto;
    private String passportPhoto;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String residenceDetails;
    private String county;
    private String occupation;
    private String businessName;
    private String businessLocation;
    private Float monthlyIncome;
    private Integer creditScore;

    private List<CustomerCollateralsDto> customerCollaterals;
    private List<GuarantorDto> guarantors;
    private List<GuarantorCollateralDto> guarantorCollaterals;
    private List<RefereeDto> referees;
}

