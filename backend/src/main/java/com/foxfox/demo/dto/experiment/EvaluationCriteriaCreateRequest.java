package com.foxfox.demo.dto.experiment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class EvaluationCriteriaCreateRequest {

    // 选择题：正确选项（存代码或标识），支持单/多选
    private String correctOptions;
    private Boolean multiple;

    // 填空题：标准答案列表
    private String fillAnswers;

    // 编程题：测试用例
    @Valid
    private String testCases;

    public String getCorrectOptions() {
        return correctOptions;
    }

    public void setCorrectOptions(String correctOptions) {
        this.correctOptions = correctOptions;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public String getFillAnswers() {
        return fillAnswers;
    }

    public void setFillAnswers(String fillAnswers) {
        this.fillAnswers = fillAnswers;
    }

    public String getTestCases() {
        return testCases;
    }

    public void setTestCases(String testCases) {
        this.testCases = testCases;
    }

    public static class CodeTestCaseDto {
        @NotEmpty
        private String input;
        @NotEmpty
        private String expectedOutput;
        private Long timeoutMillis;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getExpectedOutput() {
            return expectedOutput;
        }

        public void setExpectedOutput(String expectedOutput) {
            this.expectedOutput = expectedOutput;
        }

        public Long getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(Long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }
    }
}