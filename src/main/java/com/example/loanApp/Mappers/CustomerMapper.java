package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateRefereeRequest;
import com.example.loanApp.dtos.CustomerRequest;
import com.example.loanApp.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "nationalId", source = "nationalId")
    @Mapping(target = "county", source = "county")
    Customer toEntity(CustomerRequest customer);
    List<CustomerRequest> toDtoList(List<Customer> customers);
}
