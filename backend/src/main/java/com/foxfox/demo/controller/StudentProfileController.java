package com.foxfox.demo.controller;

import com.foxfox.demo.dto.StudentExperimentHistoryItem;
import com.foxfox.demo.dto.StudentProfileResponse;
import com.foxfox.demo.dto.UpdateStudentProfileRequest;
import com.foxfox.demo.service.StudentProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @GetMapping("/{userId}/profile")
    public StudentProfileResponse getProfile(@PathVariable int userId) {
        return studentProfileService.getProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    public StudentProfileResponse updateProfile(@PathVariable int userId,
                                                @RequestBody UpdateStudentProfileRequest req) {
        return studentProfileService.updateProfile(userId, req);
    }

    @GetMapping("/{userId}/history")
    public List<StudentExperimentHistoryItem> history(@PathVariable int userId) {
        return studentProfileService.listHistory(userId);
    }
}