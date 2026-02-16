package com.example.loanApp.service;

import com.example.loanApp.dtos.EditUserRequest;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.UserDto;
import com.example.loanApp.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface UserDetailsService {
    GenericResponse<List<UserDto>> getUsers(String search, Pageable pageable);

    GenericResponse<UserDto> getUsersById(int id);

    User getUserById(Integer userId) throws UsernameNotFoundException;

    void editUser(EditUserRequest request);

    void deactivateUser(int id);
}
