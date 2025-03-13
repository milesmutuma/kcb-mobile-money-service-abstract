package com.kcb.mobilemoney.controller;

import com.kcb.mobilemoney.api.dto.PaymentRequest;
import com.kcb.mobilemoney.api.dto.PaymentResponse;
import com.kcb.mobilemoney.model.Transaction;
import com.kcb.mobilemoney.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment API", description = "APIs for processing B2C mobile money payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('PAYMENT_INITIATOR')")
    @Operation(summary = "Initiate a B2C payment")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Payment initiation request received for phone number: {}", request.getPhoneNumber());
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{referenceNumber}")
    @PreAuthorize("hasRole('PAYMENT_VIEWER')")
    @Operation(summary = "Get transaction status by reference number")
    public ResponseEntity<Transaction> getTransaction(
            @PathVariable @NotBlank(message = "Reference number is required") String referenceNumber) {
        log.info("Fetching transaction details for reference number: {}", referenceNumber);
        Transaction transaction = paymentService.getTransaction(referenceNumber);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle payment callback from mobile money provider")
    public ResponseEntity<Void> handleCallback(
            @RequestParam @NotBlank(message = "Reference number is required") String referenceNumber,
            @RequestParam @NotBlank(message = "Result code is required") String resultCode,
            @RequestParam(required = false) String resultDescription) {
        log.info("Callback received for transaction: {}", referenceNumber);
        paymentService.handleCallback(referenceNumber, resultCode, resultDescription);
        return ResponseEntity.ok().build();
    }
} 