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
import com.foxfox.demo.dto.experiment.StudentCurrentExperimentItem;
import com.foxfox.demo.repository.ExperimentAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final EvaluationRepository evaluationRepository;
    private final ExperimentAssignmentRepository experimentAssignmentRepository;

    public StudentProfileServiceImpl(UserRepository userRepository,
                                     StudentProfileRepository studentProfileRepository,
                                     StudentAnswerRepository studentAnswerRepository,
                                     EvaluationRepository evaluationRepository,
                                     ExperimentAssignmentRepository experimentAssignmentRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.evaluationRepository = evaluationRepository;
        this.experimentAssignmentRepository = experimentAssignmentRepository;
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

        // 预聚合：每个发布实验下已完成(最终提交 或 获得满分)的阶段集合
        Map<Long, Set<Integer>> finishedStageIdsMap = new HashMap<>();
        for (StudentAnswer ans : answers) {
            if (ans.getPublishedExperiment() == null || ans.getPublishedExperiment().getId() == null || ans.getStage() == null) continue;
            long pubId = ans.getPublishedExperiment().getId();
            boolean finished = ans.isFinalSubmit();
            if (!finished) {
                var eval = ans.getEvaluation();
                if (eval == null) {
                    eval = evaluationRepository.findByAnswer(ans).orElse(null);
                }
                if (eval != null && ans.getStage().getMaxScore() != null && eval.getScore() != null
                        && eval.getScore().equals(ans.getStage().getMaxScore())) {
                    finished = true;
                }
            }
            if (finished) {
                finishedStageIdsMap.computeIfAbsent(pubId, k -> new HashSet<>()).add(ans.getStage().getId());
            }
        }

        return answers.stream()
                .map(ans -> {
                    StudentExperimentHistoryItem item = new StudentExperimentHistoryItem();
                    if (ans.getPublishedExperiment() != null && ans.getPublishedExperiment().getId() != null) {
                        item.setPublishedExperimentId(ans.getPublishedExperiment().getId().longValue());
                    }
                    if (ans.getStage() != null && ans.getStage().getExperiment() != null) {
                        if (ans.getStage().getExperiment().getId() != null) {
                            item.setExperimentId(ans.getStage().getExperiment().getId().longValue());
                        }
                        item.setExperimentTitle(ans.getStage().getExperiment().getTitle());
                        if (ans.getStage().getExperiment().getStages() != null) {
                            item.setTotalStages(ans.getStage().getExperiment().getStages().size());
                        }
                    } else if (ans.getPublishedExperiment() != null) {
                        if (ans.getPublishedExperiment().getId() != null) {
                            item.setExperimentId(ans.getPublishedExperiment().getId().longValue());
                        }
                        Experiment exp = ans.getPublishedExperiment().getExperiment();
                        String title = (exp != null ? exp.getTitle() : null);
                        item.setExperimentTitle(title != null ? title : "实验#" + ans.getPublishedExperiment().getId());
                        if (exp != null && exp.getStages() != null) {
                            item.setTotalStages(exp.getStages().size());
                        }
                    }
                    item.setAnswerId(ans.getId() == null ? null : ans.getId().longValue());
                    item.setSubmittedAt(ans.getSubmittedAt() != null ? ans.getSubmittedAt() : ans.getSavedAt());
                    item.setLatestScore(latestScore(ans));
                    if (item.getPublishedExperimentId() != null) {
                        Set<Integer> fs = finishedStageIdsMap.get(item.getPublishedExperimentId());
                        if (fs != null) item.setFinishedStages(fs.size());
                    }
                    return item;
                })
                .sorted(Comparator.comparing(
                        StudentExperimentHistoryItem::getSubmittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentCurrentExperimentItem> listCurrentExperiments(int userId) {
        // 1. 校验学生存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生档案不存在"));

        // 2. 查出该学生可见的所有发布实验
        var publishedList = experimentAssignmentRepository.findVisiblePublishedExperimentsForUser(userId);

        // 3. 预取该学生所有答案，用于统计完成阶段数（包含未 final 但已满分）
        List<StudentAnswer> answers = studentAnswerRepository.findByUserId(userId);

        return publishedList.stream()
                .map(p -> {
                    StudentCurrentExperimentItem item = new StudentCurrentExperimentItem();
                    item.setPublishedExperimentId(p.getId());
                    if (p.getExperiment() != null) {
                        item.setExperimentId(p.getExperiment().getId());
                        item.setExperimentTitle(p.getExperiment().getTitle());
                        if (p.getExperiment().getStages() != null) {
                            item.setTotalStages(p.getExperiment().getStages().size());
                        }
                    }
                    item.setDeadline(p.getEndTime());

                    // 统计：仅 finalSubmit 的阶段视为完成（与执行页侧边栏保持一致）；去重 stageId
                    Set<Integer> finishedStageIds = answers.stream()
                            .filter(a -> a.getPublishedExperiment() != null
                                    && a.getPublishedExperiment().getId() != null
                                    && a.getPublishedExperiment().getId().equals(p.getId())
                                    && a.getStage() != null
                                    && a.isFinalSubmit())
                            .map(a -> a.getStage().getId())
                            .collect(Collectors.toSet());
                    item.setFinishedStages(finishedStageIds.size());
                    return item;
                })
                .sorted(Comparator.comparing(StudentCurrentExperimentItem::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
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