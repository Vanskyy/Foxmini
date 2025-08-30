package com.foxfox.demo.repository;

import com.foxfox.demo.model.Evaluation;
import com.foxfox.demo.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    Optional<Evaluation> findByAnswer(StudentAnswer answer);
}