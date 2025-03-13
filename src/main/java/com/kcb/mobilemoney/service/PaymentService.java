package com.kcb.mobilemoney.service;

import com.kcb.mobilemoney.api.dto.PaymentRequest;
import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.model.Transaction;

public interface PaymentService {
    PaymentResponse initiatePayment(PaymentRequest request);
    void handleCallback(String referenceNumber, String resultCode, String resultDescription);
    Transaction getTransaction(String referenceNumber);
} 