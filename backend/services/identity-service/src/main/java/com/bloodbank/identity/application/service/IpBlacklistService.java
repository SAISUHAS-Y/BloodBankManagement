package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.BlacklistIpRequest;
import com.bloodbank.identity.domain.entity.BlacklistedIp;

import java.util.List;

public interface IpBlacklistService {

    BlacklistedIp blacklistIp(BlacklistIpRequest request, String blockedBy);

    void removeIpFromBlacklist(Long id);

    void removeIpFromBlacklistByIp(String ipAddress);

    boolean isIpBlacklisted(String ipAddress);

    List<BlacklistedIp> getAllBlacklistedIps();
}
