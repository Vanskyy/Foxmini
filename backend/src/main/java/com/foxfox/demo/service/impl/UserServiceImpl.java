package com.foxfox.demo.service.impl;

import com.foxfox.demo.dto.LoginRequest;
import com.foxfox.demo.dto.RegisterRequest;
import com.foxfox.demo.dto.UserResponse;
import com.foxfox.demo.model.*;
import com.foxfox.demo.repository.StudentProfileRepository;
import com.foxfox.demo.repository.TeacherProfileRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    public UserServiceImpl(UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           TeacherProfileRepository teacherProfileRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 基本校验
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        if (request.getRole() == null) {
            throw new IllegalArgumentException("角色不能为空");
        }

        // 创建用户（明文密码）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        userRepository.save(user);

        // 角色附加信息
        switch (request.getRole()) {
            case STUDENT -> {
                if (request.getStudentId() != null && !request.getStudentId().isBlank()) {
                    if (studentProfileRepository.existsByStudentId(request.getStudentId())) {
                        throw new IllegalArgumentException("学生学号已存在");
                    }
                    StudentProfile sp = new StudentProfile();
                    sp.setUser(user);
                    sp.setStudentId(request.getStudentId());
                    sp.setClassName(request.getClassName());
                    sp.setMajor(request.getMajor());
                    sp.setGrade(request.getGrade());
                    studentProfileRepository.save(sp);
                }
            }
            case TEACHER -> {
                if (request.getTeacherId() != null && !request.getTeacherId().isBlank()) {
                    if (teacherProfileRepository.existsByTeacherId(request.getTeacherId())) {
                        throw new IllegalArgumentException("教师工号已存在");
                    }
                    TeacherProfile tp = new TeacherProfile();
                    tp.setUser(user);
                    tp.setTeacherId(request.getTeacherId());
                    tp.setDepartment(request.getDepartment());
                    tp.setTitle(request.getTitle());
                    teacherProfileRepository.save(tp);
                }
            }
            default -> {
                // 其他角色无需附加
            }
        }
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        String key = request.getUsernameOrEmail();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("用户名或邮箱不能为空");
        }
        User user = userRepository.findByUsernameOrEmail(key, key)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 明文比对（危险示例）
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setRealName(user.getRealName());
        resp.setRole(user.getRole());
        resp.setAvatar(user.getAvatar());
        if (user.getStudentProfile() != null) {
            resp.setStudentId(user.getStudentProfile().getStudentId());
        }
        if (user.getTeacherProfile() != null) {
            resp.setTeacherId(user.getTeacherProfile().getTeacherId());
        }
        return resp;
    }
}