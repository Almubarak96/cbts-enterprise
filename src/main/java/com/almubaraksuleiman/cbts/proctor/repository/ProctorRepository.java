package com.almubaraksuleiman.cbts.proctor.repository;

import com.almubaraksuleiman.cbts.proctor.model.Proctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Repository
public interface ProctorRepository extends JpaRepository<Proctor, Long> {
    Optional<Proctor> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Proctor> findByEmail(String email);

}