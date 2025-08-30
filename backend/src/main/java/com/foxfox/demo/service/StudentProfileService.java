package com.foxfox.demo.service;

import com.foxfox.demo.dto.StudentExperimentHistoryItem;
import com.foxfox.demo.dto.StudentProfileResponse;
import com.foxfox.demo.dto.UpdateStudentProfileRequest;

import java.util.List;

public interface StudentProfileService {
    StudentProfileResponse getProfile(int userId);
    StudentProfileResponse updateProfile(int userId, UpdateStudentProfileRequest req);
    List<StudentExperimentHistoryItem> listHistory(int userId);
}