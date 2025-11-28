package com.dhrubok.taskmaster.persistence.auth.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignUpRequest {
    @NotBlank
    private String username;

    @NotBlank(message = "You must provide a name")
    private String fullName;

    @NotBlank(message = "You must provide a email")
    @Email(message = "Provide a valid email")
    private String email;

    @NotBlank(message = "Must be provide a password")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character"
    )
    private String password;
}
