package com.kcb.mobilemoney.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kcb.mobilemoney.api.dto.PaymentRequest;
import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.model.TransactionStatus;
import com.kcb.mobilemoney.providers.MobileMoneyProvider;
import com.kcb.mobilemoney.providers.MobileMoneyProviderRegistry;
import com.kcb.mobilemoney.repository.TransactionRepository;
import com.kcb.mobilemoney.service.impl.PaymentServiceImpl;

import javax.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MobileMoneyProvider mobileMoneyProvider;

    @Mock
    private MobileMoneyProviderRegistry providerRegistry;

    @Mock
    private CallbackService callbackService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                transactionRepository,
                notificationService,
                providerRegistry,
                callbackService
        );
    }

    @Test
    void initiatePayment_Success() {
        // Arrange
        PaymentRequest request = new PaymentRequest();
        request.setPhoneNumber("254712345678");
        request.setAmount(new BigDecimal("100.00"));
        request.setProvider(PaymentProvider.MPESA);

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .referenceNumber("REF123")
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .provider(request.getProvider())
                .status(TransactionStatus.PENDING)
                .build();

        when(providerRegistry.getProvider(PaymentProvider.MPESA)).thenReturn(mobileMoneyProvider);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(mobileMoneyProvider.validatePhoneNumber(request.getPhoneNumber())).thenReturn(true);

        // Act
        PaymentResponse response = paymentService.initiatePayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.PROCESSING, response.getStatus());
        verify(mobileMoneyProvider).processPayment(any(Transaction.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void initiatePayment_InvalidPhoneNumber() {
        // Arrange
        PaymentRequest request = new PaymentRequest();
        request.setPhoneNumber("invalid");
        request.setAmount(new BigDecimal("100.00"));
        request.setProvider(PaymentProvider.MPESA);

        when(providerRegistry.getProvider(PaymentProvider.MPESA)).thenReturn(mobileMoneyProvider);
        when(mobileMoneyProvider.validatePhoneNumber(request.getPhoneNumber())).thenReturn(false);

        // Act
        PaymentResponse response = paymentService.initiatePayment(request);

        // Assert
        assertEquals(TransactionStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Invalid phone number"));
        verify(notificationService, never()).sendFailureNotification(any(Transaction.class), anyString());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void handleCallback_Success() {
        // Arrange
        String referenceNumber = "REF123";
        String resultCode = "00";
        String resultDescription = "Success";

        Transaction transaction = Transaction.builder()
                .id(1L)
                .referenceNumber(referenceNumber)
                .status(TransactionStatus.PROCESSING)
                .build();

        when(transactionRepository.findByReferenceNumber(referenceNumber))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        paymentService.handleCallback(referenceNumber, resultCode, resultDescription);

        // Assert
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(resultCode, transaction.getResultCode());
        assertEquals(resultDescription, transaction.getResultDescription());
        verify(notificationService).sendTransactionNotification(any(Transaction.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void handleCallback_TransactionNotFound() {
        // Arrange
        String referenceNumber = "INVALID_REF";
        when(transactionRepository.findByReferenceNumber(referenceNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleCallback(referenceNumber, "00", "Success"));
        verify(transactionRepository).findByReferenceNumber(referenceNumber);
        verify(notificationService, never()).sendTransactionNotification(any());
        verify(transactionRepository, never()).save(any());
    }
} 