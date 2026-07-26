package com.bloodbank.notification.application.service;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.notification.domain.entity.NotificationTemplate;
import com.bloodbank.notification.domain.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateByCode(String code) {
        return templateRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate not found with code: " + code));
    }

    public String render(String templateText, Map<String, Object> variables) {
        if (templateText == null || variables == null || variables.isEmpty()) {
            return templateText;
        }
        String rendered = templateText;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            rendered = rendered.replace(placeholder, value);
        }
        return rendered;
    }
}
