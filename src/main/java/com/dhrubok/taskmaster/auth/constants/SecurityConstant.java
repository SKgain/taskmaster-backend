package com.dhrubok.taskmaster.auth.constants;

import java.util.Base64;

public final class SecurityConstant {

    public static final String JWT_SECRET = Base64.getEncoder().encodeToString(
            "MY_SUPER_SECRET_KEY_12345678901234567890".getBytes()
    );
    public static final String JWT_ALGORITHM = "HmacSHA256";
    public static final long JWT_EXPIRATION_MILLIS = 1000 * 60 * 60 * 10; // 10 hours
}