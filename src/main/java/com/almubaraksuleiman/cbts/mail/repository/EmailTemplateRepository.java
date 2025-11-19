package com.almubaraksuleiman.cbts.mail.repository;


import com.almubaraksuleiman.cbts.mail.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA repository for EmailTemplate.
 * findByNameAndActiveTrue returns only active templates (admin can deactivate).
 */
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByNameAndActiveTrue(String name);
}
