package com.almubaraksuleiman.cbts.mail.service;


import com.almubaraksuleiman.cbts.mail.model.EmailVariable;
import com.almubaraksuleiman.cbts.mail.repository.EmailVariableRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailVariableService {
    private final EmailVariableRepository repository;

    public EmailVariableService(EmailVariableRepository repository) {
        this.repository = repository;
    }

    /**
     * Load all email variables into a Map<String, Object>
     */
    public Map<String, Object> getAllVariables() {
        return repository.findAll()
                .stream()
                .collect(Collectors.toMap(EmailVariable::getKey, EmailVariable::getValue));
    }
}
