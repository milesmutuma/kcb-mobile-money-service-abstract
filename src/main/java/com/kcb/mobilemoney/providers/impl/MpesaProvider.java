package com.kcb.mobilemoney.providers.impl;

import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.providers.MobileMoneyProvider;
import org.springframework.stereotype.Service;

import com.kcb.mobilemoney.model.Transaction;

import java.security.Provider;

@Service
public class MpesaProvider implements MobileMoneyProvider {
    
    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.MPESA;
    }

    @Override
    public boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber != null && 
               phoneNumber.matches("^254(7[0-9]|1[0-1])[0-9]{7}$");
    }

    @Override
    public void processPayment(Transaction transaction) {
        // Implementation for processing M-PESA payment
        // This would include integration with M-PESA API
    }
} 