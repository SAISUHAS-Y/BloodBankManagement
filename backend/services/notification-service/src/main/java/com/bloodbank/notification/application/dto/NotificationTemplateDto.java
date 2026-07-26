package com.bloodbank.notification.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDto {

    @NotBlank(message = "Template code is required")
    private String code;

    @NotBlank(message = "Channel is required")
    private String channel; // EMAIL, SMS, PUSH

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body template is required")
    private String bodyTemplate;

    @Builder.Default
    private boolean active = true;
}
