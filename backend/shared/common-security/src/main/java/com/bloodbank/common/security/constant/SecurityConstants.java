package com.bloodbank.common.security.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {

    public static final String ROLES_CLAIM = "roles";
    public static final String PERMISSIONS_CLAIM = "permissions";
    public static final String USER_ID_CLAIM = "userId";

    // Standard Token Lifetimes
    public static final long ACCESS_TOKEN_EXPIRATION_MS = 86400000L; // 24 Hours
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L; // 7 Days
}
