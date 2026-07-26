package com.bloodbank.notification.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePreviewResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String templateCode;
    private String subject;
    private String body;
}
