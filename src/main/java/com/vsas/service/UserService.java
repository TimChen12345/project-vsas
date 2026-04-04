package com.vsas.service;

import com.vsas.dto.AuthResponse;
import com.vsas.dto.ChangePasswordRequest;
import com.vsas.dto.LoginRequest;
import com.vsas.dto.RegisterRequest;
import com.vsas.dto.UpdateProfileRequest;
import com.vsas.dto.UserProfileResponse;

public interface UserService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getProfile(String username);

    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
