package com.kcb.mobilemoney.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends PaymentException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Resource Not Found");
    }
} 