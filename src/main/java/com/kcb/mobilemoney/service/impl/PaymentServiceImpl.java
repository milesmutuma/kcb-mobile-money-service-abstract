package com.kcb.mobilemoney.service.impl;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kcb.mobilemoney.api.dto.PaymentRequest;
import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.model.TransactionStatus;
import com.kcb.mobilemoney.providers.MobileMoneyProvider;
import com.kcb.mobilemoney.providers.MobileMoneyProviderRegistry;
import com.kcb.mobilemoney.repository.TransactionRepository;
import com.kcb.mobilemoney.service.CallbackService;
import com.kcb.mobilemoney.service.NotificationService;
import com.kcb.mobilemoney.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final MobileMoneyProviderRegistry providerRegistry;
    private final CallbackService callbackService;


    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for phone number: {}", request.getPhoneNumber());

        String referenceNumber = generateReferenceNumber();
        Transaction transaction = Transaction.builder()
                .referenceNumber(referenceNumber)
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .provider(request.getProvider())
                .status(TransactionStatus.PENDING)
                .callbackUrl(request.getCallbackUrl())
                .clientRequestId(request.getRequestId())
                .callbackUsername(request.getCallbackUsername())
                .callbackPassword(request.getCallbackPassword())
                .build();

        try {
            MobileMoneyProvider provider = providerRegistry.getProvider(request.getProvider());
            
            if (!provider.validatePhoneNumber(request.getPhoneNumber())) {
                return PaymentResponse.builder()
                        .referenceNumber(referenceNumber)
                        .status(TransactionStatus.FAILED)
                        .message("Invalid phone number format for provider: " + request.getProvider())
                        .build();
            }

            transaction.setStatus(TransactionStatus.PROCESSING);
            transaction = transactionRepository.save(transaction);
            provider.processPayment(transaction);

            return PaymentResponse.builder()
                    .referenceNumber(referenceNumber)
                    .status(TransactionStatus.PROCESSING)
                    .message("Payment processing initiated successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setResultDescription(e.getMessage());
            transactionRepository.save(transaction);
            notificationService.sendFailureNotification(transaction, e.getMessage());

            return PaymentResponse.builder()
                    .referenceNumber(referenceNumber)
                    .status(TransactionStatus.FAILED)
                    .message("Payment processing failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public void handleCallback(String referenceNumber, String resultCode, String resultDescription) {
        log.info("Processing callback for transaction: {}", referenceNumber);

        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + referenceNumber));

        transaction.setResultCode(resultCode);
        transaction.setResultDescription(resultDescription);
        transaction.setStatus("00".equals(resultCode) ? TransactionStatus.COMPLETED : TransactionStatus.FAILED);

        transaction = transactionRepository.save(transaction);
        notificationService.sendTransactionNotification(transaction);
        
        // Forward the callback to the client's callback URL if provided
        callbackService.forwardCallback(transaction);
    }

    @Override
    public Transaction getTransaction(String referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + referenceNumber));
    }

    private String generateReferenceNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
} 