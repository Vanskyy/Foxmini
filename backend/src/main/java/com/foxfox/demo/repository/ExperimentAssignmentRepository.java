package com.foxfox.demo.repository;

import com.foxfox.demo.model.ExperimentAssignment;
import com.foxfox.demo.model.AssignmentTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExperimentAssignmentRepository extends JpaRepository<ExperimentAssignment, Integer> {

    List<ExperimentAssignment> findByPublishedExperiment_Id(Integer publishId);

    List<ExperimentAssignment> findByPublishedExperiment_IdAndTargetType(Integer publishId,
                                                                         AssignmentTargetType targetType);

    // 个人定向
    @Query("select a from ExperimentAssignment a " +
           "where a.publishedExperiment.id = :publishId " +
           "and a.targetType = 'INDIVIDUAL' and a.targetId = :userId")
    List<ExperimentAssignment> findIndividualForUser(Integer publishId, Integer userId);

    // 小组定向
    @Query("select a from ExperimentAssignment a " +
           "where a.publishedExperiment.id = :publishId " +
           "and a.targetType = 'GROUP' and a.targetId in :groupIds")
    List<ExperimentAssignment> findGroupAssignments(Integer publishId, List<Integer> groupIds);

    // 全体
    @Query("select a from ExperimentAssignment a " +
           "where a.publishedExperiment.id = :publishId " +
           "and a.targetType = 'ALL'")
    List<ExperimentAssignment> findGlobalAssignments(Integer publishId);

    // 汇总指定用户可见分配
    @Query("select distinct a from ExperimentAssignment a " +
           "left join GroupMember gm on a.targetType = 'GROUP' and a.targetId = gm.group.id " +
           "where a.publishedExperiment.id = :publishId and (" +
           "      a.targetType = 'ALL' " +
           "   or (a.targetType = 'INDIVIDUAL' and a.targetId = :userId) " +
           "   or (a.targetType = 'GROUP' and gm.user.id = :userId))")
    List<ExperimentAssignment> findVisibleAssignmentsForUser(Integer publishId, Integer userId);
}