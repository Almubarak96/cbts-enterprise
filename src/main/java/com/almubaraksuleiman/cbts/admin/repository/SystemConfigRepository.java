package com.almubaraksuleiman.cbts.admin.repository;

import com.almubaraksuleiman.cbts.admin.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
*/

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findByKeyName(String keyName);
}
