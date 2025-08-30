package com.foxfox.demo.service.impl;

import com.foxfox.demo.dto.TeacherProfileResponse;
import com.foxfox.demo.dto.UpdateTeacherProfileRequest;
import com.foxfox.demo.model.TeacherProfile;
import com.foxfox.demo.model.User;
import com.foxfox.demo.repository.TeacherProfileRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.TeacherProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherProfileServiceImpl implements TeacherProfileService {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public TeacherProfileServiceImpl(UserRepository userRepository,
                                     TeacherProfileRepository teacherProfileRepository) {
        this.userRepository = userRepository;
        this.teacherProfileRepository = teacherProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherProfileResponse getProfile(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        TeacherProfile profile = teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("教师档案不存在"));
        return toResp(user, profile);
    }

    @Override
    @Transactional
    public TeacherProfileResponse updateProfile(int userId, UpdateTeacherProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        TeacherProfile profile = teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("教师档案不存在"));

        if (request.getTitle() != null) { try { profile.getClass().getMethod("setTitle", String.class).invoke(profile, request.getTitle()); } catch (Exception ignored) {} }
        if (request.getDepartment() != null) { try { profile.getClass().getMethod("setDepartment", String.class).invoke(profile, request.getDepartment()); } catch (Exception ignored) {} }
        if (request.getTeacherId() != null) { try { profile.getClass().getMethod("setTeacherId", String.class).invoke(profile, request.getTeacherId()); } catch (Exception ignored) {} }
        if (request.getRealName() != null) { user.setRealName(request.getRealName()); }
        if (request.getEmail() != null) { user.setEmail(request.getEmail()); }

        userRepository.saveAndFlush(user);
        teacherProfileRepository.saveAndFlush(profile);
        return toResp(user, profile);
    }

    private TeacherProfileResponse toResp(User user, TeacherProfile profile) {
        TeacherProfileResponse r = new TeacherProfileResponse();
        r.setUserId(user.getId());
        r.setTeacherProfileId(profile.getId());
        r.setUsername(user.getUsername());
        r.setRealName(user.getRealName());
        r.setEmail(user.getEmail());
        r.setAvatar(user.getAvatar());
        // 使用 profile 的时间戳（若需要同时返回用户的也可新增字段）
        try { Object title = profile.getClass().getMethod("getTitle").invoke(profile); r.setTitle(title == null ? null : title.toString()); } catch (Exception ignored) {}
        try { Object dept = profile.getClass().getMethod("getDepartment").invoke(profile); r.setDepartment(dept == null ? null : dept.toString()); } catch (Exception ignored) {}
        try { Object tid = profile.getClass().getMethod("getTeacherId").invoke(profile); r.setTeacherId(tid == null ? null : tid.toString()); } catch (Exception ignored) {}
        try { Object created = profile.getClass().getMethod("getCreatedAt").invoke(profile); if(created!=null) r.setCreatedAt((java.time.LocalDateTime) created); } catch (Exception ignored) {}
        try { Object updated = profile.getClass().getMethod("getUpdatedAt").invoke(profile); if(updated!=null) r.setUpdatedAt((java.time.LocalDateTime) updated); } catch (Exception ignored) {}
        return r;
    }
}