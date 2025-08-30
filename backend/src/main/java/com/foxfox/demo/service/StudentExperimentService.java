
package com.foxfox.demo.service;

import com.foxfox.demo.dto.EvaluationAttemptDTO;
import com.foxfox.demo.dto.EvaluationHistoryResponse;
import com.foxfox.demo.dto.experiment.*;
import com.foxfox.demo.dto.SubmitAnswerRequest;
import com.foxfox.demo.model.StudentAnswer;
import com.foxfox.demo.model.User;
import com.foxfox.demo.repository.StudentAnswerRepository;
import com.foxfox.demo.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


public interface StudentExperimentService {

    ExperimentExecutionInitResponse init(Integer publishedExperimentId, Integer userId);

    ExperimentDetailResponse getDetail(Integer publishedExperimentId, Integer userId);

    ExperimentProgressResponse getProgress(Integer publishedExperimentId, Integer userId);

    StudentAnswerResponse getStageAnswer(Integer publishedExperimentId, Integer stageId, Integer userId);

    StudentAnswerResponse saveDraft(StudentAnswerSaveRequest request, Integer userId);

    StudentAnswerResponse finalSubmit(StudentAnswerFinalSubmitRequest request, Integer userId);

    /**
     * 提交(草稿或最终, 由 request.finalSubmit 标识)并立即自动评测, 返回最新一次评测 attempt.
     * 若是最终提交, 需要在实现中写入最终提交时间并锁定后续不可再修改(按业务需求).
     */
    EvaluationAttemptDTO submitAndEvaluate(Integer publishedExperimentId,
                                           Integer stageId,
                                           SubmitAnswerRequest request,
                                           Integer userId);

    /**
     * 获取该学生在某已发布实验某阶段的全部评测历史 (JSON 数组展开).
     */
    EvaluationHistoryResponse getEvaluationHistory(Integer publishedExperimentId,
                                                   Integer stageId,
                                                   Integer userId);


}