
package com.currencypackage; //package declaration
////necessary JUnit and Mockito packages to ensure unit testing and mocking dependencies respectively.
import static org.junit.jupiter.api.Assertions.*; // imports static assertions from JUnit5
import static org.mockito.Mockito.*;//imports Mockito methods like when and verify for mocking behavior in unit tests.
import org.junit.jupiter.api.BeforeEach;//Provides the @BeforeEach annotation to set up test-specific configurations before each test.
import org.junit.jupiter.api.Test;//Allows marking methods as unit tests using the @Test annotation.
import org.mockito.Mock;//Marks a variable as a mock object (a simulated external dependency).
import org.mockito.MockitoAnnotations;//Initializes mock objects
import java.time.LocalDateTime;//Enables working with timestamps for audit logs.
public class CurrencyClass { //Declares the main test class for CurrencyConversionModule. It contains all the unit tests.
    protected CurrencyConversion conversionModule; //test module
    @Mock
    private ExternalServiceRate mockExternalServiceRate; //Declares a mock object of ExternalRateService to simulate fetching exchange rates.
    static final double DEFAULT_EXCHANGE_RATE = 1.1;
    //Specifies a fallback exchange rate used when the external service is unavailable.
    @BeforeEach //Ensures this method runs before every test to set up required objects.
    void setUp() {
        MockitoAnnotations.openMocks(this); //Initializes all mock objects annotated with @Mock.
        conversionModule = new CurrencyConversion(mockExternalRateService, CurrentExchangeRate);
        //Instantiates the CurrencyConversion, injecting the mock external service and the current exchange rate.
    }
//Valid Transaction test
@Test //Marks this as a unit test.
void testValidTransaction() {
    // Mocking external service response
    when(mockExternalRateService.getExchangeRate("USD", "EUR")).thenReturn(0.5);
// Simulates the external service returning an exchange rate of 0.5 for converting USD to EUR.

    // Performing conversion
    Transaction transaction = conversionModule.convertCurrency("USD", "EUR", 50);
//Calls the method under test, converting 50 USD to EUR.

    // Assertions for conversion result
    assertNotNull(transaction);//Ensures the transaction object is created.
    assertEquals(85.0, transaction.getConvertedAmount(), 0.002);
    //Verifies the converted amount is correct, allowing for a small precision difference (0.002).
//Asserts all transaction details are accurate: original currency, target currency, amount, and exchange rate.
    assertEquals("USD", transaction.getOriginalCurrency());
    assertEquals("EUR", transaction.getTargetCurrency());
    assertEquals(50, transaction.getOriginalAmount());
    assertEquals(0.5, transaction.getExchangeRate());

    // Verify audit logging
    assertTrue(conversionModule.getAuditLogs().contains(transaction));
    //Ensures the transaction is logged successfully.
}
//Invalid amount testing
    @Test
    void testInvalidAmountThrowsException() {
        //assertThrows: Verifies that an exception is thrown for a negative amount.
        //IllegalArgumentException: Expected exception type.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionModule.convertCurrency("USD", "EUR", -100);
        });
       //Verifies the exception message and ensures no transaction is logged.
        // Verify exception message
        assertEquals("Amount must be positive", exception.getMessage());

        // Verify no audit logging
        assertTrue(conversionModule.getAuditLogs().isEmpty());
    }
    //invalid currency code
    @Test
    void testInvalidCurrencyCodeThrowsException() {
        //thenThrow: Simulates the external service throwing an exception for an unsupported currency code.
        when(mockExternalRateService.getExchangeRate("INVALID", "EUR"))
                .thenThrow(new IllegalArgumentException("Unsupported currency code"));
//Confirms the exception message and checks no transaction is logged.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionModule.convertCurrency("INVALID", "EUR", 100);
        });

        // Verify exception message
        assertEquals("Unsupported currency code", exception.getMessage());

        // Verify no audit logging
        assertTrue(conversionModule.getAuditLogs().isEmpty());
    }
    //test for unavailable service scenario
    @Test
    void testUnavailableExternalServiceUsesDefaultRate() {
        //RuntimeException: Simulates the external service being unavailable.
        when(mockExternalRateService.getExchangeRate("USD", "EUR"))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Performing conversion with default rate
        //Verifies the conversion succeeds using the default rate.
        Transaction transaction = conversionModule.convertCurrency("USD", "EUR", 100);

        // Assertions for conversion with default rate
        //Confirms the default exchange rate is used and the converted amount is correct.
        assertNotNull(transaction);
        assertEquals(110.0, transaction.getConvertedAmount(), 0.01);
        assertEquals(DEFAULT_EXCHANGE_RATE, transaction.getExchangeRate());

        // Verify audit logging
        assertTrue(conversionModule.getAuditLogs().contains(transaction));
    }

}
