package com.foxfox.demo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxfox.demo.dto.EvaluationAttemptDTO;
import com.foxfox.demo.dto.EvaluationHistoryResponse;
import com.foxfox.demo.model.Evaluation;
import com.foxfox.demo.model.EvaluationBy;
import com.foxfox.demo.model.StudentAnswer;
import com.foxfox.demo.repository.EvaluationRepository;
import com.foxfox.demo.service.EvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluationServiceImpl(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    @Transactional
    public Evaluation autoEvaluate(StudentAnswer answer) {
        Evaluation evaluation = evaluationRepository.findByAnswer(answer)
                .orElseGet(() -> {
                    Evaluation e = new Evaluation();
                    e.setAnswer(answer);
                    e.setEvaluatedBy(EvaluationBy.SYSTEM);
                    return e;
                });

        List<EvaluationAttemptDTO> attempts = loadAttempts(evaluation);

        EvaluationAttemptDTO attempt = new EvaluationAttemptDTO();
        attempt.setAttempt(attempts.size() + 1);
        attempt.setEvaluatedAt(LocalDateTime.now());
        attempt.setEvaluatedBy("SYSTEM");

        int score = simpleScore(answer);
        attempt.setScore(score);
        attempt.setFeedback(score == 100 ? "全部通过" : "存在错误，继续加油");
        attempt.setCases(dummyCases());

        attempts.add(attempt);

        evaluation.setScore(score);
        evaluation.setTestResults(serializeAttempts(attempts));
        return evaluationRepository.save(evaluation);
    }

    @Override
    public EvaluationHistoryResponse getHistory(StudentAnswer answer) {
        EvaluationHistoryResponse resp = new EvaluationHistoryResponse();
        resp.setAnswerId(answer.getId());
        evaluationRepository.findByAnswer(answer).ifPresent(ev -> {
            resp.setLatestScore(ev.getScore());
            resp.setAttempts(loadAttempts(ev));
        });
        return resp;
    }

    private List<EvaluationAttemptDTO> loadAttempts(Evaluation evaluation) {
        if (evaluation.getTestResults() == null || evaluation.getTestResults().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(evaluation.getTestResults(),
                    new TypeReference<List<EvaluationAttemptDTO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String serializeAttempts(List<EvaluationAttemptDTO> attempts) {
        try {
            return objectMapper.writeValueAsString(attempts);
        } catch (Exception e) {
            return "[]";
        }
    }

    private int simpleScore(StudentAnswer answer) {
        if (answer.getAnswerContent() != null && answer.getAnswerContent().contains("OK")) {
            return 100;
        }
        return 60;
    }

    private List<EvaluationAttemptDTO.TestCaseResult> dummyCases() {
        List<EvaluationAttemptDTO.TestCaseResult> list = new ArrayList<>();
        EvaluationAttemptDTO.TestCaseResult c1 = new EvaluationAttemptDTO.TestCaseResult();
        c1.setName("case1");
        c1.setStatus("PASS");
        c1.setTimeMs(5L);
        list.add(c1);

        EvaluationAttemptDTO.TestCaseResult c2 = new EvaluationAttemptDTO.TestCaseResult();
        c2.setName("case2");
        c2.setStatus("FAIL");
        c2.setMessage("期望=3 实际=4");
        c2.setTimeMs(7L);
        list.add(c2);
        return list;
    }
}