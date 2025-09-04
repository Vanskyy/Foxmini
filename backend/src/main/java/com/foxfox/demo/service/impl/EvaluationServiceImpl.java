package com.foxfox.demo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxfox.demo.dto.EvaluationAttemptDTO;
import com.foxfox.demo.dto.EvaluationHistoryResponse;
import com.foxfox.demo.model.Evaluation;
import com.foxfox.demo.model.EvaluationBy;
import com.foxfox.demo.model.StudentAnswer;
import com.foxfox.demo.model.ExperimentStage;
import com.foxfox.demo.model.EvaluationCriteria;
import com.foxfox.demo.model.StageType;
import com.foxfox.demo.repository.EvaluationRepository;
import com.foxfox.demo.service.EvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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

        // 新评分逻辑
        int score = evaluateAnswer(answer);
        attempt.setScore(score);
        attempt.setFeedback(score == getMaxScore(answer) ? "通过" : "未通过");
        attempt.setCases(buildCaseResults(answer, score));

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

    // ========== 新增核心评测方法 ==========
    private int evaluateAnswer(StudentAnswer answer) {
        ExperimentStage stage = answer.getStage();
        int max = getMaxScore(answer);
        if (stage == null) return 0;
        EvaluationCriteria criteria = stage.getEvaluationCriteria();
        StageType type = stage.getType();
        if (type == null) return 0;
        switch (type) {
            case CHOICE:
                return evaluateChoice(answer, criteria, max);
            case FILL:
                return evaluateFill(answer, criteria, max);
            case CODE:
                return evaluateCode(answer, criteria, max);
            case TEXT:
            default:
                // 主观题暂不自动判分，返回0
                return 0;
        }
    }

    private int getMaxScore(StudentAnswer answer) {
        try {
            ExperimentStage stage = answer.getStage();
            return stage == null || stage.getMaxScore() == null ? 0 : stage.getMaxScore();
        } catch (Exception e) {
            return 0;
        }
    }

    private int evaluateChoice(StudentAnswer answer, EvaluationCriteria criteria, int max) {
        if (criteria == null) return 0;
        String correct = normalizeChoice(criteria.getCorrectAnswer());
        String student = normalizeChoice(answer.getAnswerContent());
        if (correct != null && correct.equals(student)) return max;
        return 0;
    }

    private String normalizeChoice(String s) {
        if (s == null) return null;
        String compact = s.replaceAll("[^A-Za-z]", "").toUpperCase();
        // 多选需要排序
        return compact.chars().sorted()
                .collect(StringBuilder::new, (sb, c) -> sb.append((char) c), StringBuilder::append)
                .toString();
    }

    private int evaluateFill(StudentAnswer answer, EvaluationCriteria criteria, int max) {
        if (criteria == null) return 0;
        String std = safe(criteria.getCorrectAnswer());
        String stu = safe(answer.getAnswerContent());
        if (std.isEmpty() || stu.isEmpty()) return 0;
        // 支持多空：尝试用 || 分隔或换行分隔
        String[] stdParts = splitFill(std);
        String[] stuParts = splitFill(stu);
        if (stdParts.length == 0) return 0;
        if (stdParts.length == stuParts.length) {
            boolean all = true;
            for (int i = 0; i < stdParts.length; i++) {
                if (!stdParts[i].equalsIgnoreCase(stuParts[i])) {
                    all = false;
                    break;
                }
            }
            if (all) return max;
            return 0;
        } else {
            // 长度不一致直接判错
            return 0;
        }
    }

    private String[] splitFill(String s) {
        if (s == null) return new String[0];
        if (s.contains("||")) return Arrays.stream(s.split("\\|\\|"))
                .map(String::trim).filter(x -> !x.isEmpty()).toArray(String[]::new);
        return Arrays.stream(s.split("\n"))
                .map(String::trim).filter(x -> !x.isEmpty()).toArray(String[]::new);
    }

    private int evaluateCode(StudentAnswer answer, EvaluationCriteria criteria, int max) {
        // 占位：当前只做最简单判定；后续可解析 testCases JSON 并执行。
        String code = safe(answer.getCodeContent());
        if (code.contains("OK") || code.contains("pass") || code.contains("PASS")) {
            return max;
        }
        return 0;
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private List<EvaluationAttemptDTO.TestCaseResult> buildCaseResults(StudentAnswer answer, int score) {
        List<EvaluationAttemptDTO.TestCaseResult> list = new ArrayList<>();
        ExperimentStage stage = answer.getStage();
        String type = stage == null || stage.getType() == null ? "UNKNOWN" : stage.getType().name();
        EvaluationAttemptDTO.TestCaseResult c = new EvaluationAttemptDTO.TestCaseResult();
        c.setName(type + "-auto");
        c.setStatus(score == getMaxScore(answer) ? "PASS" : "FAIL");
        c.setTimeMs(1L);
        if (!"PASS".equals(c.getStatus())) {
            c.setMessage("自动评测未通过");
        }
        list.add(c);
        return list;
    }
}