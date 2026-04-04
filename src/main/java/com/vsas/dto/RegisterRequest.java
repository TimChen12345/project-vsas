package com.vsas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username is required (this is what you type when signing in).")
    @Size(
            min = 3,
            max = 64,
            message = "Username must be between 3 and 64 characters (you entered a value outside this range).")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(
            min = 6,
            max = 128,
            message =
                    "Password must be between 6 and 128 characters. Choose a longer password for security (at least 6 characters).")
    private String password;

    /** Custom unique ID key — no two accounts may share this. */
    @NotBlank(message = "ID key is required. It is your public identifier used in filters and lists.")
    @Size(
            min = 2,
            max = 64,
            message = "ID key must be between 2 and 64 characters.")
    @Pattern(
            regexp = "^[a-zA-Z0-9][a-zA-Z0-9._-]*$",
            message =
                    "ID key must start with a letter or digit, then only letters, digits, dot (.), underscore (_), or hyphen (-). "
                            + "No spaces.")
    private String idKey;

    @NotBlank(message = "Full name is required.")
    @Size(max = 200, message = "Full name must be at most 200 characters.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(
            message =
                    "Email must be valid, e.g. name@example.com. Check that you included @ and a domain (such as .com).")
    @Size(max = 255, message = "Email must be at most 255 characters.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Size(max = 32, message = "Phone number must be at most 32 characters.")
    @Pattern(
            regexp = "^[+0-9][0-9\\s().-]{5,31}$",
            message =
                    "Phone must start with + or a digit, be 6–32 characters total, and may include spaces, dots, parentheses, or hyphens "
                            + "(example: +61 400 000 000).")
    private String phoneNumber;

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
}
