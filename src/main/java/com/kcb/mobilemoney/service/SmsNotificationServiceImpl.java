package com.kcb.mobilemoney.service;

import com.kcb.mobilemoney.model.Transaction;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationServiceImpl implements NotificationService {

    @Override
    public void sendTransactionNotification(Transaction transaction) {
        // send SMS notification
    }

    @Override
    public void sendFailureNotification(Transaction transaction, String errorMessage) {
        // send SMS Failure notification
    }
}
