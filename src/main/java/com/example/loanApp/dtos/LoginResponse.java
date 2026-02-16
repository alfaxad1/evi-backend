package com.example.loanApp.dtos;

import com.example.loanApp.enums.ResponseStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expirationTime;
    private UserData userData;
}
