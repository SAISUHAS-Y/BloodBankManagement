package com.bloodbank.user.domain.repository;

import com.bloodbank.user.domain.entity.DonorNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorNoteRepository extends JpaRepository<DonorNote, Long> {
    List<DonorNote> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    Page<DonorNote> findByDonorId(Long donorId, Pageable pageable);
}
