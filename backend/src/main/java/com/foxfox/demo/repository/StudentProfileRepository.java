package com.foxfox.demo.repository;

import com.foxfox.demo.model.StudentProfile;
import com.foxfox.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Integer> {

    Optional<StudentProfile> findByUser(User user);

    Optional<StudentProfile> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);
}