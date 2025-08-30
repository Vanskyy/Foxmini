package com.foxfox.demo.service;

import com.foxfox.demo.dto.EvaluationHistoryResponse;
import com.foxfox.demo.model.Evaluation;
import com.foxfox.demo.model.StudentAnswer;

public interface EvaluationService {

    Evaluation autoEvaluate(StudentAnswer answer);

    EvaluationHistoryResponse getHistory(StudentAnswer answer);
}