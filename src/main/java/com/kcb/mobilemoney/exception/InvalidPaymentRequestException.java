package com.kcb.mobilemoney.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentRequestException extends PaymentException {
    public InvalidPaymentRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Invalid Payment Request");
    }
} 