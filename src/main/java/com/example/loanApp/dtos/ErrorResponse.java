package com.example.loanApp.dtos;

import com.example.loanApp.enums.ResponseStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ErrorResponse {
    ResponseStatusEnum status;
    String message;
}
