package com.kcb.mobilemoney.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;



    @Test
    void protectedEndpoints_NoAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/payments/REF123"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/payments"))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @WithMockUser(roles = "PAYMENT_INITIATOR")
    void initiatePayment_WithInitiatorRole_Success() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType("application/json")
                .content("{\"phoneNumber\":\"254712345678\",\"amount\":100.00,\"provider\":\"MPESA\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PAYMENT_VIEWER")
    void initiatePayment_WithViewerRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType("application/json")
                .content("{\"phoneNumber\":\"254712345678\",\"amount\":100.00,\"provider\":\"MPESA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PAYMENT_INITIATOR")
    void getTransaction_WithInitiatorRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/payments/REF123"))
                .andExpect(status().isForbidden());
    }
} 