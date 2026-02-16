package com.example.loanApp.controller;

import com.example.loanApp.dtos.CreateCustomerRequest;
import com.example.loanApp.dtos.CustomerDetailsDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.entities.Customer;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.CustomerServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.loanApp.utility.ResponseHandler;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class CustomerController {
    @Autowired
    private CustomerServicesImpl customerService;

    //create a customer
    @PostMapping(value = "/customers", consumes = "multipart/form-data")
    public ResponseEntity<?> createCustomer(
            @RequestPart("customerDetails") CreateCustomerRequest customerRequest,
            @RequestPart(value = "nationalIdPhoto", required = false) MultipartFile nationalIdPhoto,
            @RequestPart(value = "passportPhoto", required = false) MultipartFile passportPhoto,
            @RequestPart(value = "guarantorIdPhoto0", required = false) MultipartFile guarantorIdPhoto0,
            @RequestPart(value = "guarantorPassPhoto0", required = false) MultipartFile guarantorPassPhoto0
    ){
        customerService.createCustomer(customerRequest, nationalIdPhoto, passportPhoto, guarantorIdPhoto0, guarantorPassPhoto0);
        return ResponseHandler.responseBuilder("created successfully", HttpStatus.CREATED, null);
    }

    @GetMapping("/customers")
    public ResponseEntity<Object> getCustomers(
            @RequestParam Integer userId,
            @RequestParam(required = false) String search,
            @RequestParam int page,
            @RequestParam int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        GenericResponse<List<CustomerDetailsDto>> response = customerService.getAllCustomers( userId, search, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    //get customer by id
    @GetMapping("/customer/{id}")
    public ResponseEntity<Object> getCustomer(@PathVariable Integer id){
        Customer customer = customerService.getCustomer(id);
        if(customer != null)
            return ResponseHandler.responseBuilder("customer found", HttpStatus.OK, customer);
        return ResponseHandler.responseBuilder("customer doesn't exist", HttpStatus.NOT_FOUND, null);
    }

    @GetMapping("/all-customers")
    public ResponseEntity<?> getAllCustomers(@RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok().body(customerService.findCustomers(pageable));
    }
}
