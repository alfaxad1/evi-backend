package com.example.loanApp.Mappers;

import com.example.loanApp.dtos.UserDto;
import com.example.loanApp.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "monthlyDisbursementTarget", target = "disbursementTarget")
    @Mapping(source = "monthlyCollectionTarget", target = "collectionTarget")
    @Mapping(source = "createdAt", target = "userSince")
    UserDto toUserDto(User user);
}
