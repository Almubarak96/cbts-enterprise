package com.almubaraksuleiman.cbts.security.authentication.repository;

import com.almubaraksuleiman.cbts.security.authentication.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findAllByUsername(String username);
}
