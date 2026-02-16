package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateGuarantorRequest;
import com.example.loanApp.entities.Guarantor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GuarantorsMapper {
    Guarantor toEntity(CreateGuarantorRequest guarantor);

    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "nationalId", source = "nationalId")
    List<Guarantor> toEntities(List<CreateGuarantorRequest> guarantors);
}
