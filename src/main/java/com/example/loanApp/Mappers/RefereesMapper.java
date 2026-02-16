package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.CreateRefereeRequest;
import com.example.loanApp.entities.Referee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefereesMapper {
    List<Referee> toEntities(List<CreateRefereeRequest> referees);

    List<CreateRefereeRequest> toDtoList(List<Referee> referees);

}
