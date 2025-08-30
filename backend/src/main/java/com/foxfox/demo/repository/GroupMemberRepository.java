package com.foxfox.demo.repository;

import com.foxfox.demo.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {

    List<GroupMember> findByGroup_Id(Integer groupId);

    List<GroupMember> findByUser_Id(Integer userId);

    boolean existsByGroup_IdAndUser_Id(Integer groupId, Integer userId);

    long countByGroup_Id(Integer groupId);

    @Query("select gm.group.id from GroupMember gm where gm.user.id = :userId")
    List<Integer> findGroupIdsByUser(Integer userId);

    Optional<GroupMember> findByGroup_IdAndUser_Id(Integer groupId, Integer userId);
    List<GroupMember> findByGroup_IdAndUser_IdIn(Integer groupId, Collection<Integer> userIds);

}