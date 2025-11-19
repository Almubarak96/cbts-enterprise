package com.almubaraksuleiman.cbts.admin.repository;

import com.almubaraksuleiman.cbts.admin.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Admin> findByEmail(String email);
}
