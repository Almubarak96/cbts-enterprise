package com.almubaraksuleiman.cbts.mail.repository;



import com.almubaraksuleiman.cbts.mail.model.EmailVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVariableRepository extends JpaRepository<EmailVariable, Long> {
    EmailVariable findByKey(String key);
}
