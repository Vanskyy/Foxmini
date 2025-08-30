package com.foxfox.demo.service;

import com.foxfox.demo.dto.BatchAddStudentsResult;
import com.foxfox.demo.dto.BatchRemoveStudentsResult;
import com.foxfox.demo.model.Group;
import com.foxfox.demo.model.GroupMember;
import com.foxfox.demo.model.Role;
import com.foxfox.demo.model.User;
import com.foxfox.demo.repository.GroupMemberRepository;
import com.foxfox.demo.repository.GroupRepository;
import com.foxfox.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupMoveService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupMoveService(GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addStudent(Integer groupId, Integer userId) {
        if (groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, userId)) {
            throw new IllegalStateException("学生已在分组");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        if(user.getRole() != Role.STUDENT){
            throw new IllegalStateException("只能添加学生用户");
        }
        GroupMember gm = new GroupMember();
        gm.setGroup(group);
        gm.setUser(user);
        groupMemberRepository.save(gm);
    }

    @Transactional
    public void removeStudent(Integer groupId, Integer userId) {
        GroupMember gm = groupMemberRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new EntityNotFoundException("学生不在该分组"));
        groupMemberRepository.delete(gm);
    }

    @Transactional
    public void moveStudent(Integer fromGroupId, Integer toGroupId, Integer userId) {
        if (fromGroupId.equals(toGroupId)) {
            return;
        }
        GroupMember gm = groupMemberRepository.findByGroup_IdAndUser_Id(fromGroupId, userId)
                .orElseThrow(() -> new EntityNotFoundException("学生不在原分组"));
        if (groupMemberRepository.existsByGroup_IdAndUser_Id(toGroupId, userId)) {
            throw new IllegalStateException("学生已在目标分组");
        }
        if(gm.getUser().getRole() != Role.STUDENT){
            throw new IllegalStateException("只能移动学生用户");
        }
        Group toGroup = groupRepository.findById(toGroupId)
                .orElseThrow(() -> new EntityNotFoundException("目标分组不存在"));
        gm.setGroup(toGroup);
        groupMemberRepository.save(gm);
    }
    @Transactional
    public BatchAddStudentsResult addStudentsBatch(Integer groupId, Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new BatchAddStudentsResult(List.of(), List.of(), List.of());
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));

        // 去重
        Set<Integer> distinctIds = new LinkedHashSet<>(userIds);

        // 已在分组的
        List<GroupMember> existingMembers = groupMemberRepository
                .findByGroup_IdAndUser_IdIn(groupId, distinctIds);
        Set<Integer> alreadyInGroup = existingMembers.stream()
                .map(gm -> gm.getUser().getId())
                .collect(Collectors.toSet());

        // 需要尝试新增的
        List<Integer> toAddIds = distinctIds.stream()
                .filter(id -> !alreadyInGroup.contains(id))
                .toList();

        // 查询存在的用户
        List<User> users = userRepository.findAllById(toAddIds);
        Set<Integer> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
        List<Integer> notFound = toAddIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        // 过滤非学生
        List<User> studentUsers = users.stream().filter(u -> u.getRole() == Role.STUDENT).toList();
        List<Integer> nonStudentIds = users.stream().filter(u -> u.getRole() != Role.STUDENT).map(User::getId).toList();

        // 构造并保存
        List<GroupMember> newMembers = studentUsers.stream()
                .map(u -> {
                    GroupMember gm = new GroupMember();
                    gm.setGroup(group);
                    gm.setUser(u);
                    return gm;
                })
                .toList();
        groupMemberRepository.saveAll(newMembers);

        List<Integer> success = studentUsers.stream().map(User::getId).toList();
        // 将非学生视为 notFound 的扩展（前端需提示）
        List<Integer> finalNotFound = new ArrayList<>(notFound);
        finalNotFound.addAll(nonStudentIds);
        return new BatchAddStudentsResult(success,
                new ArrayList<>(alreadyInGroup),
                finalNotFound);
    }

    @Transactional
    public BatchRemoveStudentsResult removeStudentsBatch(Integer groupId, Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new BatchRemoveStudentsResult(List.of(), List.of());
        }
        Set<Integer> distinct = new LinkedHashSet<>(userIds);
        List<GroupMember> members = groupMemberRepository.findByGroup_IdAndUser_IdIn(groupId, distinct);
        Set<Integer> inGroup = members.stream()
                .map(gm -> gm.getUser().getId())
                .collect(Collectors.toSet());
        List<Integer> notInGroup = distinct.stream()
                .filter(id -> !inGroup.contains(id))
                .toList();
        groupMemberRepository.deleteAll(members);
        return new BatchRemoveStudentsResult(new ArrayList<>(inGroup), notInGroup);
    }
}