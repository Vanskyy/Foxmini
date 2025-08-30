package com.foxfox.demo.controller;

import com.foxfox.demo.dto.*;
import com.foxfox.demo.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) { this.groupService = groupService; }

    // 列表：GET /api/teacher/groups?creatorId=xxx
    @GetMapping
    public ResponseEntity<List<GroupResponse>> listGroups(@RequestParam Integer creatorId){
        return ResponseEntity.ok(groupService.listGroups(creatorId));
    }

    // 创建：POST /api/teacher/groups?creatorId=xxx
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestParam Integer creatorId,
                                                     @RequestBody GroupCreateRequest req){
        return ResponseEntity.ok(groupService.createGroup(creatorId, req));
    }

    // 更新：PUT /api/teacher/groups/{groupId}
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable Integer groupId,
                                                     @RequestBody GroupUpdateRequest req){
        return ResponseEntity.ok(groupService.updateGroup(groupId, req));
    }

    // 删除：DELETE /api/teacher/groups/{groupId}
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Integer groupId){
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    // 成员列表：GET /api/teacher/groups/{groupId}/members
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> listMembers(@PathVariable Integer groupId){
        return ResponseEntity.ok(groupService.listMembers(groupId));
    }
}
