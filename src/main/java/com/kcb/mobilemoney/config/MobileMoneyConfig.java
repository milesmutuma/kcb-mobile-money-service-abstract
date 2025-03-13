package com.kcb.mobilemoney.config;

import com.kcb.mobilemoney.model.PaymentProvider;
import com.kcb.mobilemoney.providers.MobileMoneyProvider;
import com.kcb.mobilemoney.providers.MobileMoneyProviderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;

import java.util.Map;

@Configuration
public class MobileMoneyConfig {

    @Bean
    public MobileMoneyProviderRegistry providerRegistry(ApplicationContext applicationContext) {
        MobileMoneyProviderRegistry registry = new MobileMoneyProviderRegistry();
        
        // Get all beans that implement MobileMoneyProvider
        Map<String, MobileMoneyProvider> providers = applicationContext.getBeansOfType(MobileMoneyProvider.class);
        
        // Register each provider with its corresponding enum value
        providers.values().forEach(provider -> {
            PaymentProvider type = provider.getProvider();
            registry.registerProvider(type, provider);
        });
        
        return registry;
    }
} 