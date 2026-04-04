package com.vsas.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 200, message = "Full name must be at most 200 characters.")
    private String fullName;

    @Size(max = 64, message = "ID key must be at most 64 characters.")
    @Pattern(
            regexp = "^$|^[a-zA-Z0-9][a-zA-Z0-9._-]*$",
            message =
                    "If set, ID key must start with a letter or digit, then only letters, digits, . _ or - (no spaces). Leave blank to skip.")
    private String idKey;

    @Size(max = 255, message = "Email must be at most 255 characters.")
    @Pattern(
            regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
            message =
                    "When provided, email must be valid: local part, @, and domain with a dot (e.g. name@example.com).")
    private String email;

    @Size(max = 32, message = "Phone number must be at most 32 characters.")
    @Pattern(
            regexp = "^$|^[+0-9][0-9\\s().-]{5,31}$",
            message =
                    "Phone must start with + or a digit and be 6–32 characters (spaces, dots, parentheses allowed).")
    private String phoneNumber;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
