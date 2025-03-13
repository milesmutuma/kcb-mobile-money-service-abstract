package com.kcb.mobilemoney.service;

import com.kcb.mobilemoney.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationServiceImpl implements NotificationService {

    @Override
    public void sendTransactionNotification(Transaction transaction) {
        // send SMS notification
        // UNIMPLEMENTED
        // We can use Twilio or any other SMS service provider to send SMS
    }

    @Override
    public void sendFailureNotification(Transaction transaction, String errorMessage) {
        // send SMS Failure notification
        // UNIMPLEMENTED

        // We can use Twilio or any other SMS service provider to send SMS
    }
}
