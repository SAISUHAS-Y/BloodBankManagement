package com.bloodbank.notification.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogResponse {

    private Long id;
    private String eventId;
    private String eventType;
    private String recipientType;
    private String recipientId;
    private String channel;
    private String templateCode;
    private String status;
    private Instant sentAt;
    private String failureReason;
    private Instant createdAt;
}
