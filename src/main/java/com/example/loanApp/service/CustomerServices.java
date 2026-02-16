package com.example.loanApp.service;

import com.example.loanApp.dtos.CreateCustomerRequest;
import com.example.loanApp.dtos.CustomerDetailsDto;
import com.example.loanApp.dtos.CustomersData;
import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerServices {
    //GenericResponse<List<CustomerDetailsDto>> getAllCustomers(Pageable pageable, Integer userId);
    Customer getCustomer(Integer id);

    GenericResponse<List<CustomerDetailsDto>> getAllCustomers(
            Integer userId,
            String search,
            Pageable pageable
    );

    void createCustomer(CreateCustomerRequest customerRequest,
                        MultipartFile nationalIdPhoto,
                        MultipartFile passportPhoto,
                        MultipartFile guarantorIdPhoto,
                        MultipartFile guarantorPassPhoto);
    void deleteCustomer(Integer id);

     List<CustomersData> findCustomers(Pageable pageable);
}
