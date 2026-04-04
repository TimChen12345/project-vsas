package com.vsas.dto;

import java.time.Instant;

public class UserProfileResponse {

    private Long id;
    private String username;
    private String idKey;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Instant memberSince;
    private long scrollCount;
    /** USER or ADMIN */
    private String role;

    public UserProfileResponse() {}

    public UserProfileResponse(
            Long id,
            String username,
            String idKey,
            String fullName,
            String email,
            String phoneNumber,
            Instant memberSince,
            long scrollCount,
            String role) {
        this.id = id;
        this.username = username;
        this.idKey = idKey;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.memberSince = memberSince;
        this.scrollCount = scrollCount;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Instant getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(Instant memberSince) {
        this.memberSince = memberSince;
    }

    public long getScrollCount() {
        return scrollCount;
    }

    public void setScrollCount(long scrollCount) {
        this.scrollCount = scrollCount;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
