package com.foxfox.demo.controller;

import com.foxfox.demo.dto.TeacherProfileResponse;
import com.foxfox.demo.dto.UpdateTeacherProfileRequest;
import com.foxfox.demo.service.TeacherProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/profile")
public class TeacherProfileController {

    private final TeacherProfileService teacherProfileService;

    public TeacherProfileController(TeacherProfileService teacherProfileService) {
        this.teacherProfileService = teacherProfileService;
    }

    @GetMapping("/{userId}")
    public TeacherProfileResponse getProfile(@PathVariable int userId) {
        return teacherProfileService.getProfile(userId);
    }

    @PutMapping("/{userId}")
    public TeacherProfileResponse updateProfile(@PathVariable int userId,
                                                @RequestBody UpdateTeacherProfileRequest request) {
        return teacherProfileService.updateProfile(userId, request);
    }
}