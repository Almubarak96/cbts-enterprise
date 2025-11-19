package com.almubaraksuleiman.cbts.examiner.repository;

import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Repository
public interface ExaminerRepository extends JpaRepository<Examiner, Long> {
    Optional<Examiner> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Examiner> findByEmail(String email);

}
