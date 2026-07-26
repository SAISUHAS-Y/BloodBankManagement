package com.bloodbank.notification.infrastructure.sender;

import com.bloodbank.notification.application.sender.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("[EMAIL SENDER STUB] Sending Email to: {}\nSubject: {}\nBody: \n{}", to, subject, body);
    }
}
