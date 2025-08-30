
package com.foxfox.demo.controller;

import com.foxfox.demo.dto.SubmitAnswerRequest;
import com.foxfox.demo.dto.EvaluationAttemptDTO;
import com.foxfox.demo.dto.EvaluationHistoryResponse;
import com.foxfox.demo.dto.experiment.*;
import com.foxfox.demo.service.StudentExperimentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/experiments")
public class StudentExperimentController {

    private final StudentExperimentService studentExperimentService;

    public StudentExperimentController(StudentExperimentService studentExperimentService) {
        this.studentExperimentService = studentExperimentService;
    }

    // 获取执行初始化（详情 + 进度 + 当前阶段答案）
    @GetMapping("/{publishedExperimentId}/init")
    public ResponseEntity<ExperimentExecutionInitResponse> init(@PathVariable Integer publishedExperimentId) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(studentExperimentService.init(publishedExperimentId, userId));
    }

    // 实验详情
    @GetMapping("/{publishedExperimentId}/detail")
    public ResponseEntity<ExperimentDetailResponse> detail(@PathVariable Integer publishedExperimentId) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(studentExperimentService.getDetail(publishedExperimentId, userId));
    }

    // 进度
    @GetMapping("/{publishedExperimentId}/progress")
    public ResponseEntity<ExperimentProgressResponse> progress(@PathVariable Integer publishedExperimentId) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(studentExperimentService.getProgress(publishedExperimentId, userId));
    }

    // 某阶段答案（草稿或最终）
    @GetMapping("/{publishedExperimentId}/stages/{stageId}/answer")
    public ResponseEntity<StudentAnswerResponse> stageAnswer(@PathVariable Integer publishedExperimentId,
                                                             @PathVariable Integer stageId) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(studentExperimentService.getStageAnswer(publishedExperimentId, stageId, userId));
    }

    // 保存草稿
    @PostMapping("/answer/save")
    public ResponseEntity<StudentAnswerResponse> saveDraft(@Valid @RequestBody StudentAnswerSaveRequest request) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(studentExperimentService.saveDraft(request, userId));
    }

    // 最终提交
    @PostMapping("/answer/final-submit")
    public ResponseEntity<StudentAnswerResponse> finalSubmit(@Valid @RequestBody StudentAnswerFinalSubmitRequest request) {
        if (!request.isConfirm()) {
            return ResponseEntity.badRequest().build();
        }
        Integer userId = currentUserId();
        return ResponseEntity.ok(studentExperimentService.finalSubmit(request, userId));
    }
    //  提交并立即评测 (可用于草稿实时评测或最终提交评测)
    @PostMapping("/{publishedExperimentId}/stages/{stageId}/evaluate")
    public ResponseEntity<EvaluationAttemptDTO> submitAndEvaluate(@PathVariable Integer publishedExperimentId,
                                                                  @PathVariable Integer stageId,
                                                                  @RequestBody SubmitAnswerRequest request) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(
                studentExperimentService.submitAndEvaluate(publishedExperimentId, stageId, request, userId)
        );
    }

    //  获取评测历史
    @GetMapping("/{publishedExperimentId}/stages/{stageId}/evaluation-history")
    public ResponseEntity<EvaluationHistoryResponse> evaluationHistory(@PathVariable Integer publishedExperimentId,
                                                                       @PathVariable Integer stageId) {
        Integer userId = currentUserId();
        return ResponseEntity.ok(
                studentExperimentService.getEvaluationHistory(publishedExperimentId, stageId, userId)
        );
    }

    // 获取当前登录学生ID（示例：应从安全上下文中获取）
    private Integer currentUserId() {
        // TODO: 集成 Spring Security 后替换
        // 临时：可通过线程上下文 / SecurityContextHolder 获取
        //不考虑安全性

        return 1;
    }
}