package com.kcb.mobilemoney.service;

import com.kcb.mobilemoney.api.dto.PaymentRequest;
import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.config.TestConfig;
import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.model.TransactionStatus;
import com.kcb.mobilemoney.providers.MobileMoneyProvider;
import com.kcb.mobilemoney.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MobileMoneyProvider mobileMoneyProvider;

    private PaymentRequest validRequest;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();

        validRequest = new PaymentRequest();
        validRequest.setPhoneNumber("254712345678");
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setProvider(PaymentProvider.MPESA);
    }

    @Test
    @Transactional
    void initiatePayment_FullFlow_Success() {
        // Initiate payment
        PaymentResponse response = paymentService.initiatePayment(validRequest);
        assertNotNull(response);
        assertEquals(TransactionStatus.PROCESSING, response.getStatus());

        // Verify transaction is saved
        Optional<Transaction> savedTransaction = transactionRepository.findByReferenceNumber(response.getReferenceNumber());
        assertTrue(savedTransaction.isPresent());
        assertEquals(validRequest.getPhoneNumber(), savedTransaction.get().getPhoneNumber());
        assertEquals(validRequest.getAmount(), savedTransaction.get().getAmount());
        assertEquals(validRequest.getProvider(), savedTransaction.get().getProvider());

    }

    @Test
    @Transactional
    void handleCallback_SuccessfulTransaction() {
        // First initiate a payment
        PaymentResponse response = paymentService.initiatePayment(validRequest);
        String referenceNumber = response.getReferenceNumber();

        // Handle successful callback
        paymentService.handleCallback(referenceNumber, "00", "Success");

        // Verify transaction status
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow();
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals("00", transaction.getResultCode());
        assertEquals("Success", transaction.getResultDescription());

        // Verify notification was sent
        verify(notificationService).sendTransactionNotification(transaction);
    }

    @Test
    @Transactional
    void handleCallback_FailedTransaction() {
        // First initiate a payment
        PaymentResponse response = paymentService.initiatePayment(validRequest);
        String referenceNumber = response.getReferenceNumber();

        // Handle failed callback
        paymentService.handleCallback(referenceNumber, "01", "Insufficient funds");

        // Verify transaction status
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow();
        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        assertEquals("01", transaction.getResultCode());
        assertEquals("Insufficient funds", transaction.getResultDescription());

        // Verify notification was sent
        verify(notificationService).sendTransactionNotification(transaction);
    }

    @Test
    @Transactional
    void getTransaction_ExistingReference() {
        // First initiate a payment
        PaymentResponse response = paymentService.initiatePayment(validRequest);
        String referenceNumber = response.getReferenceNumber();

        // Get transaction
        Transaction transaction = paymentService.getTransaction(referenceNumber);
        assertNotNull(transaction);
        assertEquals(referenceNumber, transaction.getReferenceNumber());
    }

    @Test
    void getTransaction_NonExistentReference() {
        assertThrows(EntityNotFoundException.class,
                () -> paymentService.getTransaction("NON_EXISTENT_REF"));
    }

    @Test
    @Transactional
    void initiatePayment_ConcurrentTransactions() throws InterruptedException {
        // Create multiple threads to simulate concurrent requests
        int numThreads = 5;
        Thread[] threads = new Thread[numThreads];
        PaymentResponse[] responses = new PaymentResponse[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                PaymentRequest request = new PaymentRequest();
                request.setPhoneNumber("254712345678");
                request.setAmount(new BigDecimal("100.00"));
                request.setProvider(PaymentProvider.MPESA);
                responses[index] = paymentService.initiatePayment(request);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify each transaction has a unique reference number
        for (int i = 0; i < numThreads; i++) {
            for (int j = i + 1; j < numThreads; j++) {
                assertNotEquals(responses[i].getReferenceNumber(), responses[j].getReferenceNumber(),
                        "Reference numbers should be unique");
            }
        }
    }

    @Test
    @Transactional
    void initiatePayment_LargeAmount() {
        validRequest.setAmount(new BigDecimal("1000000.00")); // 1 million
        PaymentResponse response = paymentService.initiatePayment(validRequest);
        assertNotNull(response);
        assertEquals(TransactionStatus.PROCESSING, response.getStatus());
    }

    @Test
    @Transactional
    void initiatePayment_SmallAmount() {
        validRequest.setAmount(new BigDecimal("1")); // 1 kes
        PaymentResponse response = paymentService.initiatePayment(validRequest);
        assertNotNull(response);
        assertEquals(TransactionStatus.PROCESSING, response.getStatus());
    }
} 