package com.foxfox.demo.repository;

import com.foxfox.demo.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    List<Group> findByCreatedBy_Id(Integer creatorId);

    List<Group> findByNameContainingIgnoreCase(String namePart);
}