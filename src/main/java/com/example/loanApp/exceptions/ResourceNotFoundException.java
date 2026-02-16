package com.example.loanApp.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException (String message){
        super(message);
    }
}
