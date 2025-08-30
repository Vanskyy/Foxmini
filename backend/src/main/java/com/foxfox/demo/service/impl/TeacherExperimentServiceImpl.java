package com.foxfox.demo.service.impl;


import com.foxfox.demo.dto.experiment.*;
import com.foxfox.demo.model.*;
import com.foxfox.demo.repository.ExperimentRepository;
import com.foxfox.demo.repository.ExperimentResourceRepository;
import com.foxfox.demo.repository.ExperimentStageRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.TeacherExperimentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherExperimentServiceImpl implements TeacherExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ExperimentStageRepository stageRepository;
    private final ExperimentResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public TeacherExperimentServiceImpl(ExperimentRepository experimentRepository,
                                        ExperimentStageRepository stageRepository,
                                        ExperimentResourceRepository resourceRepository,
                                        UserRepository userRepository) {
        this.experimentRepository = experimentRepository;
        this.stageRepository = stageRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ExperimentCreatedResponse createExperiment(Integer creatorId, ExperimentCreateRequest req) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("教师不存在"));

        Experiment experiment = new Experiment();
        experiment.setTitle(req.getTitle());
        experiment.setDescription(req.getDescription());
        experiment.setCreator(creator);
        experiment.setStatus(req.getStatus() == null ? ExperimentStatus.DRAFT : req.getStatus());
        experiment = experimentRepository.save(experiment);

        List<Integer> stageIds = new ArrayList<>();
        if (req.getStages() != null && !req.getStages().isEmpty()) {
            int autoSeqBase = stageRepository.findMaxSequenceByExperimentId(experiment.getId());
            int index = 0;
            for (ExperimentStageCreateRequest sReq : req.getStages()) {
                ExperimentStage stage = new ExperimentStage();
                stage.setExperiment(experiment);
                stage.setTitle(sReq.getTitle());
                stage.setDescription(sReq.getDescription());
                stage.setType(sReq.getType());
                stage.setContent(sReq.getContent());
                stage.setHint(sReq.getHint());
                stage.setMaxScore(sReq.getMaxScore() == null ? 0 : sReq.getMaxScore());
                stage.setSequence(sReq.getSequence() != null ? sReq.getSequence() : autoSeqBase + (++index));

                if (sReq.getEvaluation() != null) {
                    EvaluationCriteria criteria = buildEvaluationCriteria(stage, sReq);
                    stage.setEvaluationCriteria(criteria);
                }

                stageRepository.save(stage);
                stageIds.add(stage.getId());
            }
        }

        List<Integer> resourceIds = new ArrayList<>();
        if (req.getResources() != null && !req.getResources().isEmpty()) {
            for (ExperimentResourceCreateRequest rReq : req.getResources()) {
                ExperimentResource res = new ExperimentResource();
                res.setExperiment(experiment);
                res.setName(rReq.getName());
                res.setType(rReq.getType());
                res.setUrl(rReq.getUrl());
                res.setSize(rReq.getSize());
                resourceRepository.save(res);
                resourceIds.add(res.getId());
            }
        }

        return new ExperimentCreatedResponse(experiment.getId(), stageIds, resourceIds);
    }

    private EvaluationCriteria buildEvaluationCriteria(ExperimentStage stage,
                                                       ExperimentStageCreateRequest sReq) {
        EvaluationCriteriaCreateRequest ev = sReq.getEvaluation();
        EvaluationCriteria criteria = new EvaluationCriteria();
        criteria.setStage(stage);
        if (ev.getCorrectOptions() != null) {
            criteria.setCorrectAnswer(ev.getCorrectOptions());
        }
        if (ev.getFillAnswers() != null) {
            criteria.setCorrectAnswer(ev.getFillAnswers());
        }
        if (ev.getTestCases() != null) {
            criteria.setTestCases(ev.getTestCases());
        }
        return criteria;
    }
    public ExperimentResponse update(int teacherUserId, int experimentId, ExperimentUpdateRequest req) {
        userRepository.findById(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        Experiment exp = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        verifyOwner(exp, teacherUserId);

        if (req.getTitle() != null) {
            setIfExists(exp, "setTitle", req.getTitle());
            setIfExists(exp, "setName", req.getTitle());
        }
        if (req.getDescription() != null) setIfExists(exp, "setDescription", req.getDescription());
        if (req.getObjective() != null) setIfExists(exp, "setObjective", req.getObjective());
        if (req.getStatus() != null) setIfExists(exp, "setStatus", req.getStatus());

        experimentRepository.save(exp);
        return toResp(exp);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ExperimentResponse get(int teacherUserId, int experimentId) {
        Experiment exp = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new IllegalArgumentException("实验不存在"));
        verifyOwner(exp, teacherUserId);
        return toResp(exp);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<ExperimentResponse> list(int teacherUserId, String status, String keyword, Pageable pageable) {
        userRepository.findById(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));

        ExperimentStatus st = null;
        if (status != null && !status.isBlank()) {
            try {
                st = ExperimentStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("非法状态: " + status);
            }
        }

        Page<Experiment> page;
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (st != null && hasKeyword) {
            page = experimentRepository.findByCreator_IdAndStatusAndTitleContainingIgnoreCase(
                    teacherUserId, st, keyword.trim(), pageable);
        } else if (st != null) {
            page = experimentRepository.findByCreator_IdAndStatus(teacherUserId, st, pageable);
        } else if (hasKeyword) {
            page = experimentRepository.findByCreator_IdAndTitleContainingIgnoreCase(
                    teacherUserId, keyword.trim(), pageable);
        } else {
            page = experimentRepository.findByCreator_Id(teacherUserId, pageable);
        }

        return page.map(this::toResp);
    }

    private void verifyOwner(Experiment exp, int teacherUserId) {
        Integer ownerId = readUserId(exp, "getCreator", "getOwner", "getPublisher");
        if (ownerId == null || ownerId != teacherUserId) {
            throw new IllegalArgumentException("无权操作该实验");
        }
    }

    private Integer readUserId(Object target, String... getters) {
        for (String g : getters) {
            try {
                Object u = target.getClass().getMethod(g).invoke(target);
                if (u instanceof User) return ((User) u).getId();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void setIfExists(Object target, String method, Object value) {
        if (value == null) return;
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(method) && m.getParameterCount() == 1) {
                try { m.invoke(target, value); } catch (Exception ignored) {}
                return;
            }
        }
    }

    private String readString(Object target, String... getters) {
        for (String g : getters) {
            try {
                Object v = target.getClass().getMethod(g).invoke(target);
                if (v != null) return v.toString();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private ExperimentResponse toResp(Experiment exp) {
        ExperimentResponse r = new ExperimentResponse();
        try { r.setId((Integer) exp.getClass().getMethod("getId").invoke(exp)); } catch (Exception ignored) {}
        r.setTitle(readString(exp, "getTitle", "getName"));
        r.setDescription(readString(exp, "getDescription"));
        r.setObjective(readString(exp, "getObjective"));
        r.setStatus(readString(exp, "getStatus"));
        r.setOwnerUserId(readUserId(exp, "getCreator", "getOwner", "getPublisher"));
        // 填充阶段列表
        List<ExperimentStage> stageEntities = stageRepository.findByExperiment_IdOrderBySequenceAsc(r.getId());
        r.setStages(stageEntities.stream()
                .map(ExperimentStageResponse::from)
                .collect(Collectors.toList()));
        return r;
    }
}