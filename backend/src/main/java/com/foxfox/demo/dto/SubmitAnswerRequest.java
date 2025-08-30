package com.foxfox.demo.dto;

public class SubmitAnswerRequest {
    private String answerContent;
    private String codeContent;
    private boolean finalSubmit;

    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }
    public String getCodeContent() { return codeContent; }
    public void setCodeContent(String codeContent) { this.codeContent = codeContent; }
    public boolean isFinalSubmit() { return finalSubmit; }
    public void setFinalSubmit(boolean finalSubmit) { this.finalSubmit = finalSubmit; }
}