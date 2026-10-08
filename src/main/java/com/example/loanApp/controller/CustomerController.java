package com.example.loanApp.controller;

import com.example.loanApp.dtos.CreateCustomerRequest;
import com.example.loanApp.dtos.CreateGuarantorRequest;
import com.example.loanApp.dtos.CreateRefereeRequest;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.loanApp.dtos.CustomerDetailsDto;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.TransferCustomerRequest;
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
        Integer id = customerService.createCustomer(customerRequest, nationalIdPhoto, passportPhoto, guarantorIdPhoto0, guarantorPassPhoto0);
        return ResponseHandler.responseBuilder("created successfully", HttpStatus.CREATED, Map.of("id", id));
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
        CustomerDetailsDto customer = customerService.getCustomerDetails(id);
        if(customer != null)
            return ResponseHandler.responseBuilder("customer found", HttpStatus.OK, customer);
        return ResponseHandler.responseBuilder("customer doesn't exist", HttpStatus.NOT_FOUND, null);
    }

    @GetMapping("/all-customers")
    public ResponseEntity<?> getAllCustomers(@RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok().body(customerService.findCustomers(pageable));
    }

    @PostMapping("/customer/{id}/guarantors")
    public ResponseEntity<?> addGuarantor(@PathVariable Integer id, @Valid @RequestBody CreateGuarantorRequest request) {
        customerService.addGuarantor(id, request);
        return ResponseHandler.responseBuilder("Guarantor added", HttpStatus.CREATED, null);
    }

    @PostMapping("/customer/{id}/referees")
    public ResponseEntity<?> addReferee(@PathVariable Integer id, @Valid @RequestBody CreateRefereeRequest request) {
        customerService.addReferee(id, request);
        return ResponseHandler.responseBuilder("Referee added", HttpStatus.CREATED, null);
    }

    @PutMapping("/customer/transfer")
    public ResponseEntity<?> transferCustomers(@RequestBody TransferCustomerRequest request){
        customerService.transferCustomer(request);
        return ResponseHandler.responseBuilder("customers transferred successfully", HttpStatus.OK, null);
    }
}
