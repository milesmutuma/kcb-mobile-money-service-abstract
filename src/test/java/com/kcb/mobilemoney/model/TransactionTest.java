package com.kcb.mobilemoney.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void builder_AllFields_Success() {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .resultCode("00")
                .resultDescription("Success")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals("REF123", transaction.getReferenceNumber());
        assertEquals("254712345678", transaction.getPhoneNumber());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(PaymentProvider.MPESA, transaction.getProvider());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals("00", transaction.getResultCode());
        assertEquals("Success", transaction.getResultDescription());
        assertEquals(now, transaction.getCreatedAt());
        assertEquals(now, transaction.getUpdatedAt());
    }

    @Test
    void builder_RequiredFieldsOnly_Success() {
        Transaction transaction = Transaction.builder()
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .build();

        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertEquals("REF123", transaction.getReferenceNumber());
        assertEquals("254712345678", transaction.getPhoneNumber());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(PaymentProvider.MPESA, transaction.getProvider());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertNull(transaction.getResultCode());
        assertNull(transaction.getResultDescription());
    }

    @Test
    void setters_UpdateFields_Success() {
        Transaction transaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();

        transaction.setId(1L);
        transaction.setReferenceNumber("REF123");
        transaction.setPhoneNumber("254712345678");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setProvider(PaymentProvider.MPESA);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setResultCode("00");
        transaction.setResultDescription("Success");
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        assertEquals(1L, transaction.getId());
        assertEquals("REF123", transaction.getReferenceNumber());
        assertEquals("254712345678", transaction.getPhoneNumber());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(PaymentProvider.MPESA, transaction.getProvider());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals("00", transaction.getResultCode());
        assertEquals("Success", transaction.getResultDescription());
        assertEquals(now, transaction.getCreatedAt());
        assertEquals(now, transaction.getUpdatedAt());
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        Transaction transaction1 = Transaction.builder()
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction transaction2 = Transaction.builder()
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .build();

        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        Transaction transaction1 = Transaction.builder()
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction transaction2 = Transaction.builder()
                .referenceNumber("REF456")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("200.00"))
                .provider(PaymentProvider.AIRTEL)
                .status(TransactionStatus.COMPLETED)
                .build();

        assertNotEquals(transaction1, transaction2);
        assertNotEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void toString_ContainsAllFields() {
        Transaction transaction = Transaction.builder()
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.PENDING)
                .build();

        String toString = transaction.toString();
        assertTrue(toString.contains("REF123"));
        assertTrue(toString.contains("254712345678"));
        assertTrue(toString.contains("100.00"));
        assertTrue(toString.contains("MPESA"));
        assertTrue(toString.contains("PENDING"));
    }
} 