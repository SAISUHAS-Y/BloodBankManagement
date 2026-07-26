package com.bloodbank.notification.application.sender;

public interface EmailSender {
    void sendEmail(String to, String subject, String body);
}
