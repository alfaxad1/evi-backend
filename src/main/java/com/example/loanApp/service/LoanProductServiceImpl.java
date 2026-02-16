package com.example.loanApp.service;

import com.example.loanApp.Mappers.ProductMapper;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.LoanProduct;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanProductServiceImpl implements LoanProductService {
    private final ProductRepository productRepository;
    private final ProductMapper  productMapper;

    LoanProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper){this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public void createProduct(LoanProduct loanProduct) {
        try {
            productRepository.save(loanProduct);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving product: " + e.getMessage());
        }
    }

    @Override
    public GenericResponse<List<ProductDto>> getProducts() {
        try{
            List<LoanProduct> products = productRepository.findAllProducts();
            List<ProductDto> productDtoList = productMapper.toDtoList(products);

            return GenericResponse.<List<ProductDto>>builder()
                    .data(productDtoList)
                    .message("Products fetched successfully")
                    .status(ResponseStatusEnum.SUCCESS)
                    .build();

        } catch (Exception e) {
            return GenericResponse.<List<ProductDto>>builder()
                    .message("Error retrieving products: " + e.getMessage() +" ,"+e.getCause())
                    .status(ResponseStatusEnum.ERROR)
                    .build();
        }

    }
}