package com.almubaraksuleiman.cbts.admin.service;

import com.almubaraksuleiman.cbts.admin.repository.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Slf4j
@Component
public class ConfigLoader {

    private final SystemConfigRepository systemConfigRepository;
    private final ConfigurableEnvironment environment;

    public ConfigLoader(SystemConfigRepository systemConfigRepository, ConfigurableEnvironment environment) {
        this.systemConfigRepository = systemConfigRepository;
        this.environment = environment;
    }

    @PostConstruct
    public void loadConfigs() {
        Map<String, Object> dbConfigs = new HashMap<>();
        systemConfigRepository.findAll().forEach(c -> dbConfigs.put(c.getKeyName(), c.getValue()));

        if (!dbConfigs.isEmpty()) {
            MapPropertySource dbSource = new MapPropertySource("dbConfig", dbConfigs);

            // Ensure DB configs take highest precedence
            environment.getPropertySources().addFirst(dbSource);
            log.info("Loaded {} configs from DB into Spring Environment", dbConfigs.size());
        }
    }
}
