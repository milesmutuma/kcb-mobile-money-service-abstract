package com.kcb.mobilemoney.service;

import com.kcb.mobilemoney.model.Transaction;

public interface CallbackService {
    void forwardCallback(Transaction transaction);
} 