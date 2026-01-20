package com.dhrubok.taskmaster.common.constants;

public final class ErrorCode {
    public static String ERROR_EMAIL_OR_PASSWORD_INCORRECT = "Email or password incorrect";

    public static String ERROR_VERIFICATION_TOKEN_INVALID = "Invalid verification token";
    public static String ERROR_VERIFICATION_TOKEN_EXPIRED = "Verification token expired";
    public static String ERROR_VERIFICATION_REFRESH_TOKEN_INVALID = "Invalid refresh token";
    public static String ERROR_VERIFICATION_REFRESH_TOKEN_EXPIRED = "Refresh token expired";

    public static String ERROR_ACCOUNT_IS_NOT_VERIFIED = "Account is not verified. Please check your email.";
    public static String ERROR_ACCOUNT_IS_DISABLED = "Account is disable. Please check contact to the admin.";
    public static String ERROR_ACCOUNT_ALREADY_VERIFIED = "Account is already verified. Please Sign In.";

    public static String ERROR_CAN_NOT_CHANGE_ADMIN_ROLE = "Cannot change ADMIN role";
    public static String ERROR_CAN_NOT_DEACTIVATE_MANAGER = "Cannot deactivate ADMIN users";

    public static String ERROR_USER_ALREADY_DEACTIVATE = "User is already deactivated";
    public static String ERROR_USER_ALREADY_ACTIVATE = "User is already activated";
    public static String ERROR_USER_IS_MANAGER = "User is already a MANAGER";
    public static String ERROR_USER_NOT_FOUND = "User not found";
    public static String ERROR_USER_ALREADY_EXISTS = "User already exists";
    public static String ERROR_USER_REGISTRATION_OFF = "User registration is currently stop, Please contact to admin";

    public static String ERROR_SYSTEM_UNDER_MAINTENANCE = "System is under maintenance, Please contact to admin";
}
