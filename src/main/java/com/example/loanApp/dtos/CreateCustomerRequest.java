package com.example.loanApp.dtos;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomerRequest {
    @Valid
    private CustomerRequest  customerDetails;

    @Valid
    private List<CreateCustomerCollateralRequest> collaterals;

    @Valid
    private List<CreateGuarantorRequest> guarantors;

    @Valid
    private List<CreateGuarantorCollateralsRequest> guarantorCollaterals;

    @Valid
    private List<CreateRefereeRequest> referees;
}
