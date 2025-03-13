package com.kcb.mobilemoney.repository;

import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();

        transaction = Transaction.builder()
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_Success() {
        Transaction savedTransaction = transactionRepository.save(transaction);
        assertNotNull(savedTransaction.getId());
        assertEquals(transaction.getReferenceNumber(), savedTransaction.getReferenceNumber());
    }

    @Test
    void findByReferenceNumber_ExistingReference_ReturnsTransaction() {
        transactionRepository.save(transaction);

        Optional<Transaction> found = transactionRepository.findByReferenceNumber(transaction.getReferenceNumber());
        assertTrue(found.isPresent());
        assertEquals(transaction.getPhoneNumber(), found.get().getPhoneNumber());
    }

    @Test
    void findByReferenceNumber_NonExistentReference_ReturnsEmpty() {
        Optional<Transaction> found = transactionRepository.findByReferenceNumber("NON_EXISTENT");
        assertTrue(found.isEmpty());
    }

    @Test
    void save_UpdateExisting_Success() {
        // Save initial transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Update transaction
        savedTransaction.setStatus(TransactionStatus.COMPLETED);
        savedTransaction.setResultCode("00");
        savedTransaction.setResultDescription("Success");

        // Save updated transaction
        Transaction updatedTransaction = transactionRepository.save(savedTransaction);

        assertEquals(TransactionStatus.COMPLETED, updatedTransaction.getStatus());
        assertEquals("00", updatedTransaction.getResultCode());
        assertEquals("Success", updatedTransaction.getResultDescription());
    }


    @Test
    void save_NullRequiredFields_ThrowsException() {
        Transaction invalidTransaction = Transaction.builder()
                .referenceNumber(null)
                .phoneNumber(null)
                .amount(null)
                .provider(null)
                .status(null)
                .createdAt(null)
                .build();

        assertThrows(Exception.class, () -> transactionRepository.save(invalidTransaction));
    }

    @Test
    void save_VerifyAuditFields() {
        Transaction savedTransaction = transactionRepository.save(transaction);
        assertNotNull(savedTransaction.getCreatedAt());
        assertNotNull(savedTransaction.getUpdatedAt());
    }
} 