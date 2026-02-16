package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateGuarantorCollateralsRequest;
import com.example.loanApp.dtos.GuarantorCollateralDto;
import com.example.loanApp.entities.GuarantorCollateral;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GuarantorsCollateralsMapper {
    @Mapping(target = "itemCount", source = "itemCount")
    List<GuarantorCollateral> toEntities(List<CreateGuarantorCollateralsRequest> guarantorCollaterals);
}
