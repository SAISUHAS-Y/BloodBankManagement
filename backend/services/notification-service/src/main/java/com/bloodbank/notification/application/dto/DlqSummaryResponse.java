package com.bloodbank.notification.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlqSummaryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Integer> queueMessageCounts;
    private int totalDeadLetters;
}
