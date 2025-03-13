package com.kcb.mobilemoney.api.dto;

import com.kcb.mobilemoney.model.PaymentProvider;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$", message = "Phone number must be in the format 254XXXXXXXXX")
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment provider is required")
    private PaymentProvider provider;

    private String callbackUrl; // this is the URL where the mobile money provider will send the payment status

    private String requestId; // this is the unique identifier for the request sent by the client

    private String callbackUsername;

    private String callbackPassword;
} 