package com.kcb.mobilemoney.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public PaymentException(String message, HttpStatus status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public PaymentException(String message, HttpStatus status) {
        this(message, status, "Payment Error");
    }
} 