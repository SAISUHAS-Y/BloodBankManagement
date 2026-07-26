package com.bloodbank.notification.infrastructure.sender;

import com.bloodbank.notification.application.sender.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingSmsSender implements SmsSender {

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[SMS SENDER STUB] Sending SMS to: {}\nMessage: {}", phoneNumber, message);
    }
}
