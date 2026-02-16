package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.ProductDto;
import com.example.loanApp.entities.LoanProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "name", target = "productName")
    ProductDto toDto(LoanProduct product);
    List<ProductDto> toDtoList(List<LoanProduct> products);
}
