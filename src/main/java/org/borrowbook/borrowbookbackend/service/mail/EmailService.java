package org.borrowbook.borrowbookbackend.service.mail;

public interface EmailService {
    void sendVerificationCode(String recipientEmail, String verificationCode);
}
