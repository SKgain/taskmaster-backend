package com.dhrubok.taskmaster.auth.constants;

import java.util.Base64;

public final class SecurityConstant {

    public static final String JWT_SECRET = Base64.getEncoder().encodeToString(
            "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b32066d9317521c7f5c228122396348630325f1906236357d745428616c682701".getBytes()
    );

    public static final String JWT_ALGORITHM = "HmacSHA256";
    public static final long JWT_EXPIRATION_MILLIS = 1000 * 60 * 60; // Access Token = 60 Minutes
    public static final long REFRESH_EXPIRATION_MILLIS = 1000 * 60 * 60 * 24 * 7; // Refresh Token = 7 Days

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String[] PUBLIC_URLS = {
            "/api/auth/verify",
            "/api/auth/sign-in",
            "/api/auth/sign-up",
            "/api/auth/resend-verification",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/uploads/**",
            "/public/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/ws/**",
            "/topic/**",
            "/app/**"
    };

    public static final String VERIFICATION_URL2 = "http://localhost:8080/api/auth/verify?token=";
    public static final String VERIFICATION_URL = "/verify.html?token=";
    public static final String DASHBOARD_URL = "http://localhost:8080/dashboard";
    public static final String RESET_PASSWORD_URL = "/reset-password.html?token=";
    public static final String JWT = "bearerAuth";
}