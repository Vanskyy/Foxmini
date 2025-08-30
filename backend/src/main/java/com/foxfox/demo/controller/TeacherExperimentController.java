package com.foxfox.demo.controller;

import com.foxfox.demo.dto.experiment.ExperimentCreateRequest;
import com.foxfox.demo.dto.experiment.ExperimentCreatedResponse;
import com.foxfox.demo.dto.experiment.ExperimentResponse;
import com.foxfox.demo.dto.experiment.ExperimentUpdateRequest;
import com.foxfox.demo.service.TeacherExperimentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/experiments")
public class TeacherExperimentController {

    private final TeacherExperimentService teacherExperimentService;

    public TeacherExperimentController(TeacherExperimentService teacherExperimentService) {
        this.teacherExperimentService = teacherExperimentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperimentCreatedResponse create(@RequestParam("creatorId") Integer creatorId,
                                            @Valid @RequestBody ExperimentCreateRequest request) {
        return teacherExperimentService.createExperiment(creatorId, request);
    }

    @PutMapping("/{teacherUserId}/{experimentId}")
    public ExperimentResponse update(@PathVariable int teacherUserId,
                                     @PathVariable int experimentId,
                                     @RequestBody ExperimentUpdateRequest req) {
        return teacherExperimentService.update(teacherUserId, experimentId, req);
    }

    @GetMapping("/{teacherUserId}/{experimentId}")
    public ExperimentResponse get(@PathVariable int teacherUserId,
                                  @PathVariable int experimentId) {
        return teacherExperimentService.get(teacherUserId, experimentId);
    }

    @GetMapping("/{teacherUserId}")
    public Page<ExperimentResponse> list(@PathVariable int teacherUserId,
                                         @RequestParam(value = "status", required = false) String status,
                                         @RequestParam(value = "keyword", required = false) String keyword,
                                         @PageableDefault(size = 10)
                                         @SortDefault.SortDefaults({
                                                 @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                         }) Pageable pageable) {
        return teacherExperimentService.list(teacherUserId, status, keyword, pageable);
    }

    @DeleteMapping("/{teacherUserId}/{experimentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int teacherUserId, @PathVariable int experimentId) {
        teacherExperimentService.delete(teacherUserId, experimentId);
    }
}