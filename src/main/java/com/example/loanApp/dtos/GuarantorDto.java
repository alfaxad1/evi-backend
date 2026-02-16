package com.example.loanApp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuarantorDto {
    private Integer id;
    private String name;
    private String nationalId;
    private String phoneNumber;
    private String relationship;
    private String businessLocation;
    private String residenceDetails;
    private String idPhoto;
    private String passPhoto;

}
