package com.kcb.mobilemoney.service.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.service.CallbackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {

    private final RestTemplate restTemplate;

    @Override
    public void forwardCallback(Transaction transaction) {
        if (transaction.getCallbackUrl() == null || transaction.getCallbackUrl().isEmpty()) {
            log.debug("No callback URL provided for transaction: {}", transaction.getReferenceNumber());
            return;
        }

        try {
            PaymentResponse response = PaymentResponse.builder()
                    .referenceNumber(transaction.getReferenceNumber())
                    .status(transaction.getStatus())
                    .message(transaction.getResultDescription())
                    .requestId(transaction.getClientRequestId())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Add Basic Auth if credentials are provided
            if (transaction.getCallbackUsername() != null && transaction.getCallbackPassword() != null) {
                String auth = transaction.getCallbackUsername() + ":" + transaction.getCallbackPassword();
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + new String(encodedAuth);
                headers.set("Authorization", authHeader);
                log.debug("Added Basic Auth header for callback");
            }

            HttpEntity<PaymentResponse> request = new HttpEntity<>(response, headers);
            
            log.info("Forwarding callback for transaction {} to URL: {}", 
                    transaction.getReferenceNumber(), transaction.getCallbackUrl());
            
            restTemplate.postForEntity(transaction.getCallbackUrl(), request, String.class);
            
            log.info("Successfully forwarded callback for transaction: {}", 
                    transaction.getReferenceNumber());
        } catch (Exception e) {
            log.error("Failed to forward callback for transaction {}: {}", 
                    transaction.getReferenceNumber(), e.getMessage(), e);
        }
    }
} 