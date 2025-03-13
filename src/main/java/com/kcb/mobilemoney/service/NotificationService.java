package com.kcb.mobilemoney.service;

import com.kcb.mobilemoney.model.Transaction;

public interface NotificationService {
    void sendTransactionNotification(Transaction transaction);
    void sendFailureNotification(Transaction transaction, String errorMessage);
} 