package com.kcb.mobilemoney.config;

import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.providers.MobileMoneyProvider;
import com.kcb.mobilemoney.service.NotificationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public NotificationService mockNotificationService() {
        return mock(NotificationService.class);
    }

    @Bean
    @Primary
    public MobileMoneyProvider mockMobileMoneyProvider() {
        MobileMoneyProvider provider = mock(MobileMoneyProvider.class);
        when(provider.getProvider()).thenReturn(PaymentProvider.MPESA);
        when(provider.validatePhoneNumber(any())).thenReturn(true);
        return provider;
    }
} 