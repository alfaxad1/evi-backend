package com.example.loanApp.dtos;

import lombok.Data;

import java.util.List;

@Data
public class TransferCustomerRequest {
    private List<Integer> customerIds;
    private Integer officerId;
}
