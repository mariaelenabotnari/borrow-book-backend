package org.borrowbook.borrowbookbackend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    private Generator generator;

    @BeforeEach
    void setUp() {
        generator = new Generator();
    }

    @Test
    void generateOTP_shouldReturn6DigitString() {
        String otp = generator.generateOTP();
        
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"), "OTP should contain only digits");
    }

    @Test
    void generateOTP_shouldGenerateDifferentValues() {
        String otp1 = generator.generateOTP();
        String otp2 = generator.generateOTP();

        assertNotNull(otp1);
        assertNotNull(otp2);
    }

    @Test
    void generateSessionId_shouldReturnNonEmptyString() {
        String sessionId = generator.generateSessionId();
        
        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());
    }

    @Test
    void generateSessionId_shouldReturnBase64UrlEncodedString() {
        String sessionId = generator.generateSessionId();
        
        // Base64 URL encoding uses only alphanumeric, '-' and '_'
        assertTrue(sessionId.matches("[A-Za-z0-9_-]+"), 
            "Session ID should be Base64 URL encoded");
    }

    @Test
    void generateSessionId_shouldGenerateUniqueValues() {
        String sessionId1 = generator.generateSessionId();
        String sessionId2 = generator.generateSessionId();
        
        assertNotEquals(sessionId1, sessionId2, 
            "Session IDs should be unique");
    }
}
