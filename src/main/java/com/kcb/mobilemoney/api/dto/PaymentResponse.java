package com.kcb.mobilemoney.api.dto;

import com.kcb.mobilemoney.model.TransactionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String referenceNumber;
    private TransactionStatus status;
    private String message;
    private String requestId;
} 