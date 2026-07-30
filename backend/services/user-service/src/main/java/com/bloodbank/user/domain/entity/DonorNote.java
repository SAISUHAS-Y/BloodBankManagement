package com.bloodbank.user.domain.entity;

import com.bloodbank.common.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Entity
@Table(
    name = "donor_notes",
    indexes = {
        @Index(name = "idx_donor_note_donor", columnList = "donor_id")
    }
)
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class DonorNote extends BaseEntity {

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "author_username", nullable = false, length = 150)
    private String authorUsername;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonorNote donorNote = (DonorNote) o;
        return getId() != null && Objects.equals(getId(), donorNote.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
