package com.dhrubok.taskmaster.persistence.auth.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SignInRequest {
    @NotBlank(message = "Please provide you registered email.")
    @Email(message = "You email is not valid, please provide a valid email.")
    private String email;

    @NotBlank(message = "Password required.")
    private String password;
}
