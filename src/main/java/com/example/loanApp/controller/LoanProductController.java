package com.example.loanApp.controller;

import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.ProductDto;
import com.example.loanApp.entities.LoanProduct;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.utility.ResponseHandler;
import com.example.loanApp.service.LoanProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/product")
@CrossOrigin
public class LoanProductController {
    @Autowired
    private LoanProductServiceImpl productService;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody LoanProduct loanProduct){
        productService.createProduct(loanProduct);
        return ResponseHandler.responseBuilder("Product created successfully", HttpStatus.CREATED, null);
    }

    @GetMapping()
    public ResponseEntity<?> getProducts(){
        GenericResponse<List<ProductDto>> response = productService.getProducts();
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }
}
