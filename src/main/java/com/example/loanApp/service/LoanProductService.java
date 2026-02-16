package com.example.loanApp.service;

import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.ProductDto;
import com.example.loanApp.entities.LoanProduct;

import java.util.List;

public interface LoanProductService {
    void createProduct(LoanProduct loanProduct);

    GenericResponse<List<ProductDto>> getProducts();
}
