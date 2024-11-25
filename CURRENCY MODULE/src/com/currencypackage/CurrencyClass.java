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


// Main Test Class
public class CurrencyClass {
    private CurrencyConversion conversionModule;

    @Mock
    private ExternalServiceRate mockExternalServiceRate;

    static final double DEFAULT_EXCHANGE_RATE = 1.1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        conversionModule = new CurrencyConversion(mockExternalServiceRate, DEFAULT_EXCHANGE_RATE);
    }

    @Test
    void testValidTransaction() {
        when(mockExternalServiceRate.getExchangeRate("USD", "EUR")).thenReturn(0.5);

        Transaction transaction = conversionModule.convertCurrency("USD", "EUR", 50);

        assertNotNull(transaction);
        assertEquals(25.0, transaction.getConvertedAmount(), 0.002); // Corrected expected amount
        assertEquals("USD", transaction.getOriginalCurrency());
        assertEquals("EUR", transaction.getTargetCurrency());
        assertEquals(50, transaction.getOriginalAmount());
        assertEquals(0.5, transaction.getExchangeRate());

        assertTrue(conversionModule.getAuditLogs().contains(transaction));
    }

    @Test
    void testInvalidAmountThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionModule.convertCurrency("USD", "EUR", -100);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        assertTrue(conversionModule.getAuditLogs().isEmpty());
    }

    @Test
    void testInvalidCurrencyCodeThrowsException() {
        when(mockExternalServiceRate.getExchangeRate("INVALID", "EUR"))
                .thenThrow(new IllegalArgumentException("Unsupported currency code"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionModule.convertCurrency("INVALID", "EUR", 100);
        });

        assertEquals("Unsupported currency code", exception.getMessage());
        assertTrue(conversionModule.getAuditLogs().isEmpty());
    }

    @Test
    void testUnavailableExternalServiceUsesDefaultRate() {
        when(mockExternalServiceRate.getExchangeRate("USD", "EUR"))
                .thenThrow(new RuntimeException("Service unavailable"));

        Transaction transaction = conversionModule.convertCurrency("USD", "EUR", 100);

        assertNotNull(transaction);
        assertEquals(110.0, transaction.getConvertedAmount(), 0.01);
        assertEquals(DEFAULT_EXCHANGE_RATE, transaction.getExchangeRate());

        assertTrue(conversionModule.getAuditLogs().contains(transaction));
    }
}

// Mock Dependencies and Classes
class ExternalServiceRate {
    public double getExchangeRate(String fromCurrency, String toCurrency) {
        return 0.0; // Mock implementation
    }
}

class CurrencyConversion {
    private final ExternalServiceRate externalServiceRate;
    private final double defaultExchangeRate;
    private final List<Transaction> auditLogs = new ArrayList<>();

    public CurrencyConversion(ExternalServiceRate externalServiceRate, double defaultExchangeRate) {
        this.externalServiceRate = externalServiceRate;
        this.defaultExchangeRate = defaultExchangeRate;
    }

    public Transaction convertCurrency(String fromCurrency, String toCurrency, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        double rate;
        try {
            rate = externalServiceRate.getExchangeRate(fromCurrency, toCurrency);
        } catch (RuntimeException e) {
            rate = defaultExchangeRate;
        }

        double convertedAmount = amount * rate;
        Transaction transaction = new Transaction(fromCurrency, toCurrency, amount, rate, convertedAmount);
        auditLogs.add(transaction);
        return transaction;
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
    private final double convertedAmount;
    private final String timestamp;

    public Transaction(String originalCurrency, String targetCurrency, double originalAmount,
                       double exchangeRate, double convertedAmount) {
        this.originalCurrency = originalCurrency;
        this.targetCurrency = targetCurrency;
        this.originalAmount = originalAmount;
        this.exchangeRate = exchangeRate;
        this.convertedAmount = convertedAmount;
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

    public double getConvertedAmount() {
        return convertedAmount;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
