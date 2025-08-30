
package com.foxfox.demo.controller;

import com.foxfox.demo.dto.BatchAddStudentsRequest;
import com.foxfox.demo.dto.BatchAddStudentsResult;
import com.foxfox.demo.dto.BatchRemoveStudentsRequest;
import com.foxfox.demo.dto.BatchRemoveStudentsResult;
import com.foxfox.demo.service.GroupMoveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/groups")
public class GroupMoveController {

    private final GroupMoveService groupMoveService;

    public GroupMoveController(GroupMoveService groupMoveService) {
        this.groupMoveService = groupMoveService;
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addStudent(@PathVariable Integer groupId,
                                           @RequestParam Integer userId) {
        groupMoveService.addStudent(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeStudent(@PathVariable Integer groupId,
                                              @PathVariable Integer userId) {
        groupMoveService.removeStudent(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/move-simple")
    public ResponseEntity<Void> moveStudent(@RequestParam Integer fromGroupId,
                                            @RequestParam Integer toGroupId,
                                            @RequestParam Integer userId) {
        groupMoveService.moveStudent(fromGroupId, toGroupId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/members/batch")
    public ResponseEntity<BatchAddStudentsResult> batchAdd(@PathVariable Integer groupId,
                                                           @RequestBody BatchAddStudentsRequest req) {
        BatchAddStudentsResult result = groupMoveService.addStudentsBatch(groupId, req.getUserIds());
        return ResponseEntity.ok(result);
    }


    @PostMapping("/{groupId}/members/batch-remove")
    public ResponseEntity<BatchRemoveStudentsResult> batchRemove(@PathVariable Integer groupId,
                                                                 @RequestBody BatchRemoveStudentsRequest req) {
        BatchRemoveStudentsResult result = groupMoveService.removeStudentsBatch(groupId, req.getUserIds());
        return ResponseEntity.ok(result);
    }
}