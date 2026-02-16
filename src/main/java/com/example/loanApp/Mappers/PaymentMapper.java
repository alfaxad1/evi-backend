package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.PaymentDto;
import com.example.loanApp.entities.Repayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "paymentStatus")
    @Mapping(source = "amount", target = "paymentAmount")
    @Mapping(target = "loanId", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "loanStatus", ignore = true)
    @Mapping(target = "loanAmount", ignore = true)
    @Mapping(source = "paymentDate", target = "paymentDate", qualifiedByName = "safeDateMapping")
    PaymentDto toDto(Repayment repayment);

    @Named("safeDateMapping")
    default Date safeDateMapping(LocalDateTime paymentDate) {
        if (paymentDate == null) return null;
        return Date.from(paymentDate.atZone(ZoneId.systemDefault()).toInstant());
    }
}
