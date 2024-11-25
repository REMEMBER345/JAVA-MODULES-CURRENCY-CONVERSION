
package com.currencypackage; //package declaration
////necessary JUnit and Mockito packages to ensure unit testing and mocking dependencies respectively.
import static org.junit.jupiter.api.Assertions.*; // imports static assertions from JUnit5
import static org.mockito.Mockito.*;//imports Mockito methods like when and verify for mocking behavior in unit tests.
import org.junit.jupiter.api.BeforeEach;//Provides the @BeforeEach annotation to set up test-specific configurations before each test.
import org.junit.jupiter.api.Test;//Allows marking methods as unit tests using the @Test annotation.
import org.mockito.Mock;//Marks a variable as a mock object (a simulated external dependency).
import org.mockito.MockitoAnnotations;//Initializes mock objects
import java.time.LocalDateTime;//Enables working with timestamps for audit logs.
public class CurrencyClass { //Declares the test class for CurrencyConversionModule. It contains all the unit tests.
     CurrencyConversion conversionModule; //test module
    @Mock
    private ExternalServiceRate mockExternalServiceRate; //Declares a mock object of ExternalRateService to simulate fetching exchange rates.
    static final double CurrentExchangeRate = 1.0;
    //Specifies a fallback exchange rate used when the external service is unavailable.

   //Unauthorized access test class
   @Test
   void testUnauthorizedAccessToLogsThrowsException() {
       Exception exception = assertThrows(SecurityException.class, () -> {
           conversionModule.accessAuditLogsFromUnauthorizedModule();
       });

       // Verify exception message
       assertEquals("Unauthorized access to audit logs", exception.getMessage());
       //Ensures only authorized modules can access logs by verifying an exception is thrown for unauthorized access.
   }

  //Verifies the accuracy of audit log entries for a valid transaction.
    @Test
   void testAuditLogging() {
       when(mockExternalRateService.getExchangeRate("USD", "EUR")).thenReturn(0.85);

       // Perform a valid conversion
       conversionModule.convertCurrency("USD", "EUR", 100);

       // Retrieve the audit logs
       var logs = conversionModule.getAuditLogs();

       // Verify audit log details
       assertFalse(logs.isEmpty());
       Transaction log = logs.get(0);

       assertEquals("USD", log.getOriginalCurrency());
       assertEquals("EUR", log.getTargetCurrency());
       assertEquals(100, log.getOriginalAmount());
       assertEquals(0.85, log.getExchangeRate());
       assertNotNull(log.getTimestamp());
   }











}
