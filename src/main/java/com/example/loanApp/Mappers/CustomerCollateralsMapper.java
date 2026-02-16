package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateCustomerCollateralRequest;
import com.example.loanApp.entities.CustomerCollateral;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CustomerCollateralsMapper {
    //CustomerCollateral toEntity(CreateCustomerCollateralRequest customerCollateral);

    @Mapping(target = "itemCount", source = "itemCount")
    @Mapping(target = "itemName", source = "itemName")
    List<CustomerCollateral> toEntities(List<CreateCustomerCollateralRequest> customerCollateral);
}
