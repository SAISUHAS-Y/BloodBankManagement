package com.bloodbank.notification.application.sender;

public interface SmsSender {
    void sendSms(String phoneNumber, String message);
}
