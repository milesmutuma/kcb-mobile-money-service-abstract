package com.kcb.mobilemoney.providers;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kcb.mobilemoney.model.PaymentProvider;

@Component
public class MobileMoneyProviderRegistry {
    private final Map<PaymentProvider, MobileMoneyProvider> providers;

    public MobileMoneyProviderRegistry() {
        this.providers = new EnumMap<>(PaymentProvider.class);
    }

    public void registerProvider(PaymentProvider type, MobileMoneyProvider provider) {
        providers.put(type, provider);
    }

    public MobileMoneyProvider getProvider(PaymentProvider type) {
        MobileMoneyProvider provider = providers.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("No provider registered for type: " + type);
        }
        return provider;
    }
} 