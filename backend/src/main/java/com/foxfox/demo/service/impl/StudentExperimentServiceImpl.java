package com.foxfox.demo.service.impl;

import com.foxfox.demo.dto.EvaluationAttemptDTO;
import com.foxfox.demo.dto.EvaluationHistoryResponse;
import com.foxfox.demo.dto.SubmitAnswerRequest;
import com.foxfox.demo.dto.experiment.ExperimentDetailResponse;
import com.foxfox.demo.dto.experiment.ExperimentExecutionInitResponse;
import com.foxfox.demo.dto.experiment.ExperimentProgressResponse;
import com.foxfox.demo.dto.experiment.ExperimentStageResponse;
import com.foxfox.demo.dto.experiment.StudentAnswerFinalSubmitRequest;
import com.foxfox.demo.dto.experiment.StudentAnswerResponse;
import com.foxfox.demo.dto.experiment.StudentAnswerSaveRequest;
import com.foxfox.demo.model.Experiment;
import com.foxfox.demo.model.ExperimentStage;
import com.foxfox.demo.model.PublishedExperiment;
import com.foxfox.demo.model.StudentAnswer;
import com.foxfox.demo.model.User;
import com.foxfox.demo.repository.ExperimentStageRepository;
import com.foxfox.demo.repository.PublishedExperimentRepository;
import com.foxfox.demo.repository.StudentAnswerRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.EvaluationService;
import com.foxfox.demo.service.StudentExperimentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentExperimentServiceImpl implements StudentExperimentService {

    private final PublishedExperimentRepository publishedExperimentRepository;
    private final ExperimentStageRepository experimentStageRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final EvaluationService evaluationService;

    public StudentExperimentServiceImpl(PublishedExperimentRepository publishedExperimentRepository,
                                        ExperimentStageRepository experimentStageRepository,
                                        StudentAnswerRepository studentAnswerRepository,
                                        UserRepository userRepository,
                                        EvaluationService evaluationService) {
        this.publishedExperimentRepository = publishedExperimentRepository;
        this.experimentStageRepository = experimentStageRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
        this.evaluationService = evaluationService;
    }

    @Override
    @Transactional(readOnly = true)
    public ExperimentExecutionInitResponse init(Integer publishedExperimentId, Integer userId) {
        ExperimentDetailResponse detail = getDetail(publishedExperimentId, userId);
        ExperimentProgressResponse progress = getProgress(publishedExperimentId, userId);

        Integer currentStageId = chooseCurrentStageId(detail, progress);
        StudentAnswerResponse currentAnswer = currentStageId == null ? null :
                getStageAnswer(publishedExperimentId, currentStageId, userId);

        List<StudentAnswerResponse> allDrafts = studentAnswerRepository
                .findByUserIdAndPublishedExperimentId(userId, publishedExperimentId)
                .stream()
                .map(StudentAnswerResponse::from)
                .collect(Collectors.toList());

        return new ExperimentExecutionInitResponse(detail,
                new ExperimentProgressResponse(
                        progress.getPublishedExperimentId(),
                        progress.getTotalStages(),
                        progress.getFinishedStages(),
                        progress.getFinalSubmittedStageIds(),
                        currentStageId,
                        progress.getLastSavedAt()
                ),
                currentAnswer,
                allDrafts
        );
    }

    private Integer chooseCurrentStageId(ExperimentDetailResponse detail, ExperimentProgressResponse progress) {
        if (detail == null || detail.getStages() == null) return null;
        Set<Integer> done = progress.getFinalSubmittedStageIds() == null
                ? Collections.emptySet()
                : progress.getFinalSubmittedStageIds();
        for (ExperimentStageResponse s : detail.getStages()
                .stream()
                .sorted(Comparator.comparing(ExperimentStageResponse::getOrderNo, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList())) {
            if (!done.contains(s.getStageId())) return s.getStageId();
        }
        return detail.getStages().isEmpty() ? null
                : detail.getStages().get(detail.getStages().size() - 1).getStageId();
    }

    @Override
    @Transactional(readOnly = true)
    public ExperimentDetailResponse getDetail(Integer publishedExperimentId, Integer userId) {
        PublishedExperiment pub = publishedExperimentRepository.findById(publishedExperimentId)
                .orElseThrow(() -> new EntityNotFoundException("发布实验不存在"));
        Experiment exp = pub.getExperiment();

        List<ExperimentStage> stages = experimentStageRepository.findByExperiment_IdOrderBySequenceAsc(exp.getId());
        List<ExperimentStageResponse> stageDtos = stages.stream()
                .map(ExperimentStageResponse::from)
                .collect(Collectors.toList());
        return ExperimentDetailResponse.from(exp, pub, stageDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public ExperimentProgressResponse getProgress(Integer publishedExperimentId, Integer userId) {
        PublishedExperiment pub = publishedExperimentRepository.findById(publishedExperimentId)
                .orElseThrow(() -> new EntityNotFoundException("发布实验不存在"));

        List<ExperimentStage> stages = experimentStageRepository
                .findByExperiment_IdOrderBySequenceAsc(pub.getExperiment().getId());
        List<StudentAnswer> answers = studentAnswerRepository
                .findByUserIdAndPublishedExperimentId(userId, publishedExperimentId);

        Set<Integer> finalStageIds = answers.stream()
                .filter(StudentAnswer::isFinalSubmit)
                .map(a -> a.getStage().getId())
                .collect(Collectors.toSet());

        LocalDateTime lastSaved = answers.stream()
                .map(StudentAnswer::getSavedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new ExperimentProgressResponse(
                publishedExperimentId,
                stages.size(),
                finalStageIds.size(),
                finalStageIds,
                null,
                lastSaved
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StudentAnswerResponse getStageAnswer(Integer publishedExperimentId, Integer stageId, Integer userId) {
        return studentAnswerRepository
                .findByUserIdAndStageIdAndPublishedExperimentId(userId, stageId, publishedExperimentId)
                .map(a -> {
                    // 强制触发懒加载 evaluation (如果存在)
                    if (a.getEvaluation() != null) { a.getEvaluation().getScore(); }
                    return StudentAnswerResponse.from(a);
                })
                .orElse(null);
    }

    @Override
    public StudentAnswerResponse saveDraft(StudentAnswerSaveRequest request, Integer userId) {
        PublishedExperiment pub = publishedExperimentRepository.findById(request.getPublishedExperimentId())
                .orElseThrow(() -> new EntityNotFoundException("发布实验不存在"));
        ExperimentStage stage = experimentStageRepository.findById(request.getStageId())
                .orElseThrow(() -> new EntityNotFoundException("阶段不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        StudentAnswer answer = studentAnswerRepository
                .findByUserIdAndStageIdAndPublishedExperimentId(userId, stage.getId(), pub.getId())
                .orElseGet(() -> {
                    StudentAnswer a = new StudentAnswer();
                    a.setUser(user);
                    a.setStage(stage);
                    a.setPublishedExperiment(pub);
                    return a;
                });

        if (answer.isFinalSubmit()) {
            throw new IllegalStateException("该阶段已最终提交，不能再保存草稿");
        }
        answer.setAnswerContent(request.getAnswerContent());
        answer.setCodeContent(request.getCodeContent());
        StudentAnswer saved = studentAnswerRepository.save(answer);
        if (saved.getEvaluation() != null) saved.getEvaluation().getScore();
        return StudentAnswerResponse.from(saved);
    }

    @Override
    public StudentAnswerResponse finalSubmit(StudentAnswerFinalSubmitRequest request, Integer userId) {
        PublishedExperiment pub = publishedExperimentRepository.findById(request.getPublishedExperimentId())
                .orElseThrow(() -> new EntityNotFoundException("发布实验不存在"));
        ExperimentStage stage = experimentStageRepository.findById(request.getStageId())
                .orElseThrow(() -> new EntityNotFoundException("阶段不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        StudentAnswer answer = studentAnswerRepository
                .findByUserIdAndStageIdAndPublishedExperimentId(userId, stage.getId(), pub.getId())
                .orElseGet(() -> {
                    StudentAnswer a = new StudentAnswer();
                    a.setUser(user);
                    a.setStage(stage);
                    a.setPublishedExperiment(pub);
                    return a;
                });

        // 新增：若已有满分评测结果则禁止再次提交
        if (answer.getEvaluation() != null && stage.getMaxScore() != null) {
            Integer prev = answer.getEvaluation().getScore();
            if (prev != null && prev.equals(stage.getMaxScore())) {
                throw new IllegalStateException("该阶段已获得满分，不能再次提交");
            }
        }

        // 允许重复最终提交：覆盖内容 & 更新时间 & 继续评测 (若未满分)
        if (request.getAnswerContent() != null) answer.setAnswerContent(request.getAnswerContent());
        if (request.getCodeContent() != null) answer.setCodeContent(request.getCodeContent());
        answer.setFinalSubmit(true);
        answer.setSubmittedAt(LocalDateTime.now());

        StudentAnswer saved = studentAnswerRepository.save(answer);
        evaluationService.autoEvaluate(saved);
        if (saved.getEvaluation() != null) saved.getEvaluation().getScore();
        return StudentAnswerResponse.from(saved);
    }

    @Override
    public EvaluationAttemptDTO submitAndEvaluate(Integer publishedExperimentId,
                                                  Integer stageId,
                                                  SubmitAnswerRequest request,
                                                  Integer userId) {
        PublishedExperiment pub = publishedExperimentRepository.findById(publishedExperimentId)
                .orElseThrow(() -> new EntityNotFoundException("发布实验不存在"));
        ExperimentStage stage = experimentStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("阶段不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        StudentAnswer answer = studentAnswerRepository
                .findByUserIdAndStageIdAndPublishedExperimentId(userId, stage.getId(), pub.getId())
                .orElseGet(() -> {
                    StudentAnswer a = new StudentAnswer();
                    a.setUser(user);
                    a.setStage(stage);
                    a.setPublishedExperiment(pub);
                    return a;
                });

        // 新增：若已有满分评测结果则禁止再次提交（即时评测接口）
        if (answer.getEvaluation() != null && stage.getMaxScore() != null) {
            Integer prev = answer.getEvaluation().getScore();
            if (prev != null && prev.equals(stage.getMaxScore())) {
                throw new IllegalStateException("该阶段已获得满分，不能再次提交");
            }
        }

        // 不再阻止已 final 的再次提交；允许多次评测累积 attempts (但满分后已被阻止)
        answer.setAnswerContent(request.getAnswerContent());
        answer.setCodeContent(request.getCodeContent());
        if (request.isFinalSubmit()) {
            answer.setFinalSubmit(true);
            answer.setSubmittedAt(LocalDateTime.now());
        }
        studentAnswerRepository.save(answer);

        evaluationService.autoEvaluate(answer);

        EvaluationHistoryResponse history = evaluationService.getHistory(answer);
        List<EvaluationAttemptDTO> attempts = history.getAttempts();
        return attempts == null || attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationHistoryResponse getEvaluationHistory(Integer publishedExperimentId,
                                                          Integer stageId,
                                                          Integer userId) {
        Optional<StudentAnswer> opt = studentAnswerRepository
                .findByUserIdAndStageIdAndPublishedExperimentId(userId, stageId, publishedExperimentId);
        if (opt.isEmpty()) {
            EvaluationHistoryResponse empty = new EvaluationHistoryResponse();
            empty.setAnswerId(null);
            empty.setLatestScore(null);
            empty.setAttempts(Collections.emptyList());
            return empty;
        }
        return evaluationService.getHistory(opt.get());
    }
}