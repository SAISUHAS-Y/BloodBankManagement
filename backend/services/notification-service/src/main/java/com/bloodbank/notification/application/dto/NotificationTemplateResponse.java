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
public class NotificationTemplateResponse {

    private Long id;
    private String code;
    private String channel;
    private String subject;
    private String bodyTemplate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
