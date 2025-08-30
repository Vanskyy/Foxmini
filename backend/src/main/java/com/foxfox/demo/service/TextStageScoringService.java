
package com.foxfox.demo.service;

import com.foxfox.demo.model.*;
import com.foxfox.demo.repository.StudentAnswerRepository;
import com.foxfox.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TextStageScoringService {

    private final StudentAnswerRepository answerRepo;
    private final UserRepository userRepo;

    public TextStageScoringService(StudentAnswerRepository answerRepo,
                                   UserRepository userRepo) {
        this.answerRepo = answerRepo;
        this.userRepo = userRepo;
    }

    /**
     * 学生提交 / 更新 TEXT 阶段答案（草稿或最终）
     */
    @Transactional
    public StudentAnswer submitTextAnswer(Integer publishId,
                                          Integer stageId,
                                          Integer studentUserId,
                                          String content,
                                          boolean finalSubmit) {
        StudentAnswer answer = answerRepo
                .findByStage_IdAndUser_IdAndPublishedExperiment_Id(stageId, studentUserId, publishId)
                .orElseGet(() -> {
                    // 仅在不存在时创建（此处假设调用方已校验发布关系）
                    StudentAnswer a = new StudentAnswer();
                    User stu = new User();
                    stu.setId(studentUserId);
                    a.setUser(stu); // 仅设置 id，避免额外查询（若需安全可改成查库）
                    ExperimentStage stageRef = new ExperimentStage();
                    stageRef.setId(stageId);
                    a.setStage(stageRef);
                    PublishedExperiment pub = new PublishedExperiment();
                    pub.setId(publishId);
                    a.setPublishedExperiment(pub);
                    return a;
                });

        // 校验阶段类型（懒加载需要 stage 已被持久化；若为新建需再查一次）
        ExperimentStage stage = answer.getStage();
        if (stage.getType() != StageType.TEXT) {
            throw new IllegalStateException("仅允许 TEXT 阶段提交文本答案");
        }

        answer.setAnswerContent(content);
        if (finalSubmit) {
            answer.setFinalSubmit(true);
            answer.setSubmittedAt(LocalDateTime.now());
        }
        return answerRepo.save(answer);
    }

    /**
     * 教师评分 TEXT 阶段
     */
    @Transactional
    public StudentAnswer scoreTextAnswer(Integer publishId,
                                         Integer stageId,
                                         Integer studentUserId,
                                         Integer teacherUserId,
                                         Integer score,
                                         String comment) {
        StudentAnswer answer = answerRepo
                .findByStage_IdAndUser_IdAndPublishedExperiment_Id(stageId, studentUserId, publishId)
                .orElseThrow(() -> new EntityNotFoundException("学生答案不存在"));

        ExperimentStage stage = answer.getStage();
        if (stage.getType() != StageType.TEXT) {
            throw new IllegalStateException("仅 TEXT 阶段可人工评分");
        }
        if (score == null || score < 0 || score > stage.getMaxScore()) {
            throw new IllegalArgumentException("分值不合法");
        }

        // 权限：教师必须是实验创建者
        Experiment exp = stage.getExperiment();
        if (exp == null || exp.getCreator() == null ||
                !exp.getCreator().getId().equals(teacherUserId)) {
            throw new SecurityException("无权评分该阶段");
        }

        // 使用已有关联 Evaluation（不新增模型）
        Evaluation evaluation = answer.getEvaluation();
        if (evaluation == null) {
            evaluation = new Evaluation();
            evaluation.setAnswer(answer);
        }

        evaluation.setScore(score);
        evaluation.setFeedback(comment);
        User teacher = userRepo.findById(teacherUserId)
                .orElseThrow(() -> new EntityNotFoundException("教师不存在"));
        evaluation.setEvaluatedBy(EvaluationBy.TEACHER);
        evaluation.setTeacher(teacher);
        evaluation.setEvaluatedAt(LocalDateTime.now());

        // 由于 OneToOne(cascade = ALL) 在 answer 持久化时会级联保存 evaluation
        answer.setEvaluation(evaluation);
        return answer;
    }
}