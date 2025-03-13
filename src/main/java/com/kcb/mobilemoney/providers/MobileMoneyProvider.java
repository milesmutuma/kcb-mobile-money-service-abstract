package com.kcb.mobilemoney.providers;

import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.model.Transaction;

public interface MobileMoneyProvider {
    void processPayment(Transaction transaction);
    boolean validatePhoneNumber(String phoneNumber);
    PaymentProvider getProvider();
} 