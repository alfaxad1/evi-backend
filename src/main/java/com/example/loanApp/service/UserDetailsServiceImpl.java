package com.example.loanApp.service;

import com.example.loanApp.Mappers.UserMapper;
import com.example.loanApp.dtos.EditUserRequest;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.ResponseMetaData;
import com.example.loanApp.dtos.UserDto;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private UserMapper userMapper;
    private UserRepository userRepository;

    @Override
    public GenericResponse<List<UserDto>> getUsers(String search, Pageable pageable) {
        boolean isActive = true;
        Page<User> user = userRepository.findActiveUsers(search, pageable, isActive);
        Page<UserDto> dto = user.map(userMapper::toUserDto);

        ResponseMetaData meta = ResponseMetaData.builder()
                .page(user.getNumber())
                .totalElements(user.getTotalElements())
                .totalPages(user.getTotalPages())
                .limit(user.getSize())
                .build();

        return GenericResponse.<List<UserDto>>builder()
                .data(dto.getContent())
                .message("Users fetched successfully")
                .status(ResponseStatusEnum.SUCCESS)
                .metaData(meta)
                .build();
    }

    @Override
    public GenericResponse<UserDto> getUsersById(int id) {
        User user = userRepository.findById(id).orElse(null);
        UserDto userDto = userMapper.toUserDto(user);

        return GenericResponse.<UserDto>builder()
                .data(userDto)
                .message("User fetched successfully")
                .status(ResponseStatusEnum.SUCCESS)
                .build();
    }

    @Override
    public void editUser(EditUserRequest request){
        try {
            User user = userRepository.findById(request.getId()).orElse(null);

            assert user != null;
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setMonthlyCollectionTarget(request.getCollectionTarget());
            user.setMonthlyDisbursementTarget(request.getDisbursementTarget());

            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void deactivateUser(int id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                user.setActive(false);
                userRepository.save(user);
            }else {
                throw new UsernameNotFoundException("User not found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public User getUserById(Integer userId) throws UsernameNotFoundException {
        return userRepository.findUserById(userId);
    }
}
