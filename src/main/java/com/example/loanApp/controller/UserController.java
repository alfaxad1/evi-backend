package com.example.loanApp.controller;

import com.example.loanApp.dtos.EditUserRequest;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.SignupRequest;
import com.example.loanApp.dtos.UserDto;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.AuthService;
import com.example.loanApp.service.UserDetailsService;
import com.example.loanApp.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<User> signup(@RequestBody SignupRequest request) {
        User user = authService.signup(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping()
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<UserDto>> response = userDetailsService.getUsers(search,pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        GenericResponse<UserDto> response = userDetailsService.getUsersById(id);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody EditUserRequest request) {
        userDetailsService.editUser(request);
        return ResponseHandler.responseBuilder("user updated successfully", HttpStatus.CREATED, null);
    }

    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateUser(@RequestParam Integer id) {
        userDetailsService.deactivateUser(id);
        return ResponseHandler.responseBuilder("user deactivated successfully", HttpStatus.CREATED, null);
    }
}
