package com.foxfox.demo.service.impl;

import com.foxfox.demo.dto.StudentExperimentHistoryItem;
import com.foxfox.demo.dto.StudentProfileResponse;
import com.foxfox.demo.dto.UpdateStudentProfileRequest;
import com.foxfox.demo.model.Evaluation;
import com.foxfox.demo.model.StudentAnswer;
import com.foxfox.demo.model.StudentProfile;
import com.foxfox.demo.model.User;
import com.foxfox.demo.model.Experiment;
import com.foxfox.demo.repository.EvaluationRepository;
import com.foxfox.demo.repository.StudentAnswerRepository;
import com.foxfox.demo.repository.StudentProfileRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.StudentProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final EvaluationRepository evaluationRepository;

    public StudentProfileServiceImpl(UserRepository userRepository,
                                     StudentProfileRepository studentProfileRepository,
                                     StudentAnswerRepository studentAnswerRepository,
                                     EvaluationRepository evaluationRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生档案不存在"));
        return toResp(user, profile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateProfile(int userId, UpdateStudentProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生档案不存在"));

        boolean changed = false;
        if (req.getGrade() != null) { profile.setGrade(req.getGrade()); changed = true; }
        if (req.getMajor() != null) { profile.setMajor(req.getMajor()); changed = true; }
        if (req.getStudentId() != null) { profile.setStudentId(req.getStudentId()); changed = true; }
        if (req.getClassName() != null) { profile.setClassName(req.getClassName()); changed = true; }
        if (req.getRealName() != null) { user.setRealName(req.getRealName()); changed = true; }
        if (req.getEmail() != null) { user.setEmail(req.getEmail()); changed = true; }

        if (changed) {
            // 确保更新时间刷新：即使只改 profile 字段也更新 user.updatedAt
            user.setUpdatedAt(LocalDateTime.now());
        }

        studentProfileRepository.save(profile);
        userRepository.save(user);
        return toResp(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentExperimentHistoryItem> listHistory(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生档案不存在"));

        List<StudentAnswer> answers = studentAnswerRepository.findByUserId(user.getId());

        return answers.stream()
                .map(ans -> {
                    StudentExperimentHistoryItem item = new StudentExperimentHistoryItem();

                    // 优先通过阶段关联的实验
                    if (ans.getStage() != null && ans.getStage().getExperiment() != null) {
                        if (ans.getStage().getExperiment().getId() != null) {
                            item.setExperimentId(ans.getStage().getExperiment().getId().longValue());
                        }
                        item.setExperimentTitle(ans.getStage().getExperiment().getTitle());
                    }
                    // 回退使用已发布实验
                    else if (ans.getPublishedExperiment() != null) {
                        if (ans.getPublishedExperiment().getId() != null) {
                            item.setExperimentId(ans.getPublishedExperiment().getId().longValue());
                        }
                        Experiment exp = ans.getPublishedExperiment().getExperiment();
                        String title = (exp != null ? exp.getTitle() : null);
                        item.setExperimentTitle(title != null
                                ? title
                                : "实验#" + ans.getPublishedExperiment().getId());
                    }

                    // 答案 ID
                    item.setAnswerId(ans.getId() == null ? null : ans.getId().longValue());

                    // 提交/保存时间
                    item.setSubmittedAt(
                            ans.getSubmittedAt() != null ? ans.getSubmittedAt() : ans.getSavedAt());

                    // 最新评分
                    item.setLatestScore(latestScore(ans));
                    return item;
                })
                .sorted(Comparator.comparing(
                        StudentExperimentHistoryItem::getSubmittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private Integer latestScore(StudentAnswer answer) {
        Optional<Evaluation> ev = evaluationRepository.findByAnswer(answer);
        return ev.map(Evaluation::getScore).orElse(null);
    }

    private StudentProfileResponse toResp(User user, StudentProfile profile) {
        StudentProfileResponse r = new StudentProfileResponse();
        r.setUserId(user.getId());
        r.setStudentProfileId(profile.getId());
        r.setUsername(user.getUsername());
        r.setGrade(profile.getGrade());
        r.setMajor(profile.getMajor());
        // 新增字段赋值
        r.setRealName(user.getRealName());
        r.setEmail(user.getEmail());
        r.setStudentId(profile.getStudentId());
        r.setClassName(profile.getClassName());
        r.setUpdatedAt(user.getUpdatedAt());
        return r;
    }
}