package com.foxfox.demo.repository;

import com.foxfox.demo.model.TeacherProfile;
import com.foxfox.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Integer> {

    Optional<TeacherProfile> findByUser(User user);

    Optional<TeacherProfile> findByTeacherId(String teacherId);

    boolean existsByTeacherId(String teacherId);
}