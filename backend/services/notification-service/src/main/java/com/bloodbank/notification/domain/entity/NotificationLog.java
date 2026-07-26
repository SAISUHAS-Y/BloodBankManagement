package com.bloodbank.notification.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
public class NotificationLog extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "recipient_type", nullable = false, length = 50)
    private String recipientType; // DONOR, STAFF

    @Column(name = "recipient_id", nullable = false, length = 255)
    private String recipientId;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel; // EMAIL, SMS, PUSH

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // SENT, FAILED, SKIPPED

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationLog that = (NotificationLog) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
