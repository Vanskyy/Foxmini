
package com.foxfox.demo.controller;

import com.foxfox.demo.model.StudentAnswer;
import com.foxfox.demo.service.TextStageScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record TextAnswerSubmitRequest(String content, Boolean finalSubmit) {}
record TextScoreRequest(Integer score, String comment) {}
record TextAnswerDTO(Integer answerId,
                     Integer stageId,
                     Integer publishId,
                     Integer studentUserId,
                     Integer score,
                     String comment,
                     String content) {}

@RestController
@RequestMapping("/api/text-stages")
public class TextStageController {

    private final TextStageScoringService service;

    public TextStageController(TextStageScoringService service) {
        this.service = service;
    }

    @PostMapping("/{publishId}/{stageId}/students/{studentUserId}/answer")
    public ResponseEntity<TextAnswerDTO> submit(@PathVariable Integer publishId,
                                                @PathVariable Integer stageId,
                                                @PathVariable Integer studentUserId,
                                                @RequestBody TextAnswerSubmitRequest req) {
        StudentAnswer a = service.submitTextAnswer(
                publishId, stageId, studentUserId,
                req.content(),
                req.finalSubmit() != null && req.finalSubmit()
        );
        return ResponseEntity.ok(toDto(a));
    }

    @PostMapping("/{publishId}/{stageId}/students/{studentUserId}/score")
    public ResponseEntity<TextAnswerDTO> score(@PathVariable Integer publishId,
                                               @PathVariable Integer stageId,
                                               @PathVariable Integer studentUserId,
                                               @RequestParam Integer teacherUserId,
                                               @RequestBody TextScoreRequest req) {
        StudentAnswer a = service.scoreTextAnswer(
                publishId, stageId, studentUserId,
                teacherUserId, req.score(), req.comment()
        );
        return ResponseEntity.ok(toDto(a));
    }

    private TextAnswerDTO toDto(StudentAnswer a) {
        Integer score = a.getEvaluation() == null ? null : a.getEvaluation().getScore();
        String comment = a.getEvaluation() == null ? null : a.getEvaluation().getFeedback();        return new TextAnswerDTO(
                a.getId(),
                a.getStage().getId(),
                a.getPublishedExperiment().getId(),
                a.getUser().getId(),
                score,
                comment,
                a.getAnswerContent()
        );
    }
}