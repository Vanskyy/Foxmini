package com.foxfox.demo.service;

import com.foxfox.demo.dto.*;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(Integer teacherId){
        return groupRepository.findByCreatedBy_Id(teacherId).stream()
                .map(g -> new GroupResponse(
                        g.getId(),
                        g.getName(),
                        g.getDescription(),
                        (int) groupMemberRepository.countByGroup_Id(g.getId()),
                        g.getCreatedAt()
                )).toList();
    }

    @Transactional
    public GroupResponse createGroup(Integer teacherId, GroupCreateRequest req){
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("教师不存在"));
        if(teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN){
            throw new IllegalStateException("只有教师或管理员可以创建分组");
        }
        Group g = new Group();
        g.setName(req.getName());
        g.setDescription(req.getDescription());
        g.setCreatedBy(teacher);
        groupRepository.save(g);
        return new GroupResponse(g.getId(), g.getName(), g.getDescription(), 0, g.getCreatedAt());
    }

    @Transactional
    public GroupResponse updateGroup(Integer groupId, GroupUpdateRequest req){
        Group g = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));
        if(req.getName() != null) g.setName(req.getName());
        g.setDescription(req.getDescription());
        groupRepository.save(g);
        int count = (int) groupMemberRepository.countByGroup_Id(groupId);
        return new GroupResponse(g.getId(), g.getName(), g.getDescription(), count, g.getCreatedAt());
    }

    @Transactional
    public void deleteGroup(Integer groupId){
        Group g = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));
        groupRepository.delete(g);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> listMembers(Integer groupId){
        Group g = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("分组不存在"));
        List<GroupMember> list = groupMemberRepository.findByGroup_Id(g.getId());
        return list.stream().map(m -> new GroupMemberResponse(
                m.getUser().getId(),
                m.getUser().getUsername(),
                m.getUser().getRealName(),
                m.getJoinedAt()
        )).collect(Collectors.toList());
    }
}
