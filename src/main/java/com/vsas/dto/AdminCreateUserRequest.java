package com.vsas.dto;

import com.vsas.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminCreateUserRequest {

    @NotBlank(message = "Username is required (used when this user signs in).")
    @Size(
            min = 3,
            max = 64,
            message = "Username must be between 3 and 64 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(
            min = 6,
            max = 128,
            message = "Password must be between 6 and 128 characters.")
    private String password;

    @NotBlank(message = "ID key is required; it must be unique across all users.")
    @Size(min = 2, max = 64, message = "ID key must be between 2 and 64 characters.")
    @Pattern(
            regexp = "^[a-zA-Z0-9][a-zA-Z0-9._-]*$",
            message =
                    "ID key must start with a letter or digit, then only letters, digits, . _ or - (no spaces).")
    private String idKey;

    @NotBlank(message = "Full name is required.")
    @Size(max = 200, message = "Full name must be at most 200 characters.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(
            message =
                    "Email must be valid, e.g. name@example.com (include @ and a domain such as .com).")
    @Size(max = 255, message = "Email must be at most 255 characters.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Size(max = 32, message = "Phone number must be at most 32 characters.")
    @Pattern(
            regexp = "^[+0-9][0-9\\s().-]{5,31}$",
            message =
                    "Phone must start with + or a digit, be 6–32 characters, and may include spaces, dots, parentheses, or hyphens.")
    private String phoneNumber;

    @NotNull(message = "Role is required: choose USER or ADMIN.")
    private Role role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
