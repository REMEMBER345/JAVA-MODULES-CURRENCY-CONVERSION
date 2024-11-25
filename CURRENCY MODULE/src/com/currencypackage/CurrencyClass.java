package com.currencypackage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime; // Provides the LocalDateTime class for timestamps


public class CurrencyClass {
    private CurrencyConversion conversionModule;

    @Mock
    private ExternalServiceRate mockExternalServiceRate;

    static final double CurrentExchangeRate = 1.0;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        conversionModule = new CurrencyConversion(mockExternalServiceRate);
    }

    @Test
    void testUnauthorizedAccessToLogsThrowsException() {
        Exception exception = assertThrows(SecurityException.class, () -> {
            conversionModule.accessAuditLogsFromUnauthorizedModule();
        });

        assertEquals("Unauthorized access to audit logs", exception.getMessage());
    }

    @Test
    void testAuditLogging() {
        when(mockExternalServiceRate.getExchangeRate("USD", "EUR")).thenReturn(0.5);

        // Perform a valid conversion
        conversionModule.convertCurrency("USD", "EUR", 50);

        // Retrieve the audit logs
        List<Transaction> logs = conversionModule.getAuditLogs();

        assertFalse(logs.isEmpty());
        Transaction log = logs.get(0);

        assertEquals("USD", log.getOriginalCurrency());
        assertEquals("EUR", log.getTargetCurrency());
        assertEquals(50, log.getOriginalAmount());
        assertEquals(0.5, log.getExchangeRate());
        assertNotNull(log.getTimestamp());
    }
}

// Mock dependencies and classes for demonstration
class ExternalServiceRate {
    public double getExchangeRate(String fromCurrency, String toCurrency) {
        return 0.0; // Mock implementation
    }
}

class CurrencyConversion {
    private final ExternalServiceRate externalServiceRate;
    private final List<Transaction> auditLogs = new ArrayList<>();

    public CurrencyConversion(ExternalServiceRate externalServiceRate) {
        this.externalServiceRate = externalServiceRate;
    }

    public void accessAuditLogsFromUnauthorizedModule() {
        throw new SecurityException("Unauthorized access to audit logs");
    }

    public void convertCurrency(String fromCurrency, String toCurrency, double amount) {
        double rate = externalServiceRate.getExchangeRate(fromCurrency, toCurrency);
        auditLogs.add(new Transaction(fromCurrency, toCurrency, amount, rate));
    }

    public List<Transaction> getAuditLogs() {
        return auditLogs;
    }
}

class Transaction {
    private final String originalCurrency;
    private final String targetCurrency;
    private final double originalAmount;
    private final double exchangeRate;
    private final String timestamp;

    public Transaction(String originalCurrency, String targetCurrency, double originalAmount, double exchangeRate) {
        this.originalCurrency = originalCurrency;
        this.targetCurrency = targetCurrency;
        this.originalAmount = originalAmount;
        this.exchangeRate = exchangeRate;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
