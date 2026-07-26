package com.bloodbank.master.domain.repository;

import com.bloodbank.master.domain.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByActiveTrueOrderByNameAsc();
    List<State> findAllByOrderByNameAsc();
    Optional<State> findByCode(String code);
}
