package com.kcb.mobilemoney.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcb.mobilemoney.api.dto.PaymentRequest;
import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.model.TransactionStatus;
import com.kcb.mobilemoney.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private PaymentResponse successResponse;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        validRequest = new PaymentRequest();
        validRequest.setPhoneNumber("254712345678");
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setProvider(PaymentProvider.MPESA);

        successResponse = PaymentResponse.builder()
                .referenceNumber("REF123")
                .status(TransactionStatus.PROCESSING)
                .message("Payment processing initiated successfully")
                .build();

        mockTransaction = Transaction.builder()
                .id(1L)
                .referenceNumber("REF123")
                .phoneNumber("254712345678")
                .amount(new BigDecimal("100.00"))
                .provider(PaymentProvider.MPESA)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    @WithMockUser(roles = "PAYMENT_INITIATOR")
    void initiatePayment_ValidRequest_Success() throws Exception {
        when(paymentService.initiatePayment(any(PaymentRequest.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("REF123"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(roles = "PAYMENT_INITIATOR")
    void initiatePayment_InvalidPhoneNumber_BadRequest() throws Exception {
        validRequest.setPhoneNumber("invalid");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PAYMENT_INITIATOR")
    void initiatePayment_NegativeAmount_BadRequest() throws Exception {
        validRequest.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiatePayment_NoAuthentication_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "INVALID_ROLE")
    void initiatePayment_InvalidRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PAYMENT_VIEWER")
    void getTransaction_Success() throws Exception {
        when(paymentService.getTransaction(anyString())).thenReturn(mockTransaction);

        mockMvc.perform(get("/api/v1/payments/REF123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("REF123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "PAYMENT_VIEWER")
    void getTransaction_NotFound() throws Exception {
        when(paymentService.getTransaction(anyString()))
                .thenThrow(new EntityNotFoundException("Transaction not found"));

        mockMvc.perform(get("/api/v1/payments/INVALID_REF"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleCallback_Success() throws Exception {
        mockMvc.perform(post("/api/v1/payments/callback")
                .param("referenceNumber", "REF123")
                .param("resultCode", "00")
                .param("resultDescription", "Success"))
                .andExpect(status().isOk());
    }

    @Test
    void handleCallback_MissingRequiredParams_BadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/payments/callback")
                .param("referenceNumber", "REF123"))
                .andExpect(status().isBadRequest());
    }
} 