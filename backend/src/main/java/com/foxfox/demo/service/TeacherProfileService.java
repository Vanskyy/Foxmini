package com.foxfox.demo.service;

import com.foxfox.demo.dto.TeacherProfileResponse;
import com.foxfox.demo.dto.UpdateTeacherProfileRequest;

public interface TeacherProfileService {
    TeacherProfileResponse getProfile(int userId);
    TeacherProfileResponse updateProfile(int userId, UpdateTeacherProfileRequest request);
}