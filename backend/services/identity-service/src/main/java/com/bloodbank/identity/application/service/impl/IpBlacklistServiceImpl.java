package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.exception.DuplicateResourceException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.identity.application.dto.BlacklistIpRequest;
import com.bloodbank.identity.application.service.IpBlacklistService;
import com.bloodbank.identity.domain.entity.BlacklistedIp;
import com.bloodbank.identity.domain.repository.BlacklistedIpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpBlacklistServiceImpl implements IpBlacklistService {

    private final BlacklistedIpRepository ipRepository;

    @Override
    @Transactional
    @CacheEvict(value = "blacklisted_ips", allEntries = true)
    public BlacklistedIp blacklistIp(BlacklistIpRequest request, String blockedBy) {
        if (ipRepository.findByIpAddress(request.getIpAddress()).isPresent()) {
            throw new DuplicateResourceException("IP address is already blacklisted: " + request.getIpAddress());
        }

        BlacklistedIp entity = new BlacklistedIp();
        entity.setIpAddress(request.getIpAddress());
        entity.setReason(request.getReason() != null ? request.getReason() : "Administrative block");
        entity.setBlockedBy(blockedBy != null ? blockedBy : "SYSTEM");

        if (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) {
            entity.setExpiresAt(Instant.now().plus(request.getDurationMinutes(), ChronoUnit.MINUTES));
        } else {
            entity.setExpiresAt(null); // Permanent block
        }

        BlacklistedIp saved = ipRepository.save(entity);
        log.warn("Blacklisted IP address [{}] until [{}] by [{}]", saved.getIpAddress(), saved.getExpiresAt(), saved.getBlockedBy());
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = "blacklisted_ips", allEntries = true)
    public void removeIpFromBlacklist(Long id) {
        BlacklistedIp entity = ipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blacklisted IP not found with ID: " + id));
        ipRepository.delete(entity);
        log.info("Removed IP [{}] from blacklist", entity.getIpAddress());
    }

    @Override
    @Transactional
    @CacheEvict(value = "blacklisted_ips", allEntries = true)
    public void removeIpFromBlacklistByIp(String ipAddress) {
        BlacklistedIp entity = ipRepository.findByIpAddress(ipAddress)
                .orElseThrow(() -> new ResourceNotFoundException("IP not found in blacklist: " + ipAddress));
        ipRepository.delete(entity);
        log.info("Removed IP [{}] from blacklist", ipAddress);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "blacklisted_ips", key = "#ipAddress")
    public boolean isIpBlacklisted(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        Instant now = Instant.now();
        boolean permBlocked = ipRepository.existsByIpAddressAndExpiresAtIsNull(ipAddress);
        boolean tempBlocked = ipRepository.existsByIpAddressAndExpiresAtAfter(ipAddress, now);

        return permBlocked || tempBlocked;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistedIp> getAllBlacklistedIps() {
        return ipRepository.findAll();
    }
}
