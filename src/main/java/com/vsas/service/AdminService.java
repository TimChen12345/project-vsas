package com.vsas.service;

import com.vsas.dto.AdminCreateUserRequest;
import com.vsas.dto.AdminUserResponse;
import com.vsas.dto.ScrollStatsRowResponse;
import java.util.List;

public interface AdminService {

    List<AdminUserResponse> listUsers();

    AdminUserResponse createUser(AdminCreateUserRequest request);

    void deleteUser(Long targetUserId, String actingAdminUsername);

    List<ScrollStatsRowResponse> scrollStats();
}
