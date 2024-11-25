
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
    @BeforeEach //Ensures this method runs before every test to set up required objects.
    void setUp() {
        MockitoAnnotations.openMocks(this); //Initializes all mock objects annotated with @Mock.
        conversionModule = new CurrencyConversion(mockExternalRateService, CurrentExchangeRate);
        //Instantiates the CurrencyConversion, injecting the mock external service and the current exchange rate.
    }

}
