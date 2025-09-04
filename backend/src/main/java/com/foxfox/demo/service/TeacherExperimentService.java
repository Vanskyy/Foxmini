package com.foxfox.demo.service;

import com.foxfox.demo.dto.experiment.ExperimentCreateRequest;
import com.foxfox.demo.dto.experiment.ExperimentCreatedResponse;
import com.foxfox.demo.dto.experiment.ExperimentResponse;
import com.foxfox.demo.dto.experiment.ExperimentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherExperimentService {
    ExperimentCreatedResponse createExperiment(Integer creatorId, ExperimentCreateRequest req);
    // update: 现在支持标题/描述/状态 + stages/resources 全量替换（传空列表表示清空，不传表示不改）
    ExperimentResponse update(int teacherUserId, int experimentId, ExperimentUpdateRequest request);
    ExperimentResponse get(int teacherUserId, int experimentId);
    Page<ExperimentResponse> list(int teacherUserId, String status, String keyword, Pageable pageable);
    void delete(int teacherUserId, int experimentId);
}