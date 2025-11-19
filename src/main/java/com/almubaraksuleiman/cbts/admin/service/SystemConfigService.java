package com.almubaraksuleiman.cbts.admin.service;

import com.almubaraksuleiman.cbts.admin.model.SystemConfig;
import com.almubaraksuleiman.cbts.admin.repository.SystemConfigRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Slf4j
@Service
public class SystemConfigService {

    private final SystemConfigRepository configRepository;
    private final ConfigurableEnvironment environment;

    public SystemConfigService(SystemConfigRepository configRepository, ConfigurableEnvironment environment) {
        this.configRepository = configRepository;
        this.environment = environment;
    }

    //  Get config value by key
    public String getConfig(String key) {
        return configRepository.findByKeyName(key)
                .map(SystemConfig::getValue)
                .orElse(null);
    }

    //  Update a single config
    @Transactional
    public void updateConfig(String key, String value) {
        SystemConfig config = configRepository.findByKeyName(key)
                .orElse(new SystemConfig(key, value));
        config.setValue(value);
        configRepository.save(config);

        refreshEnvironment(Collections.singletonMap(key, value));
    }

    //  Bulk update multiple configs
    @Transactional
    public void updateConfigs(Map<String, String> configs) {
        configs.forEach((key, value) -> {
            SystemConfig config = configRepository.findByKeyName(key)
                    .orElse(new SystemConfig(key, value));
            config.setValue(value);
            configRepository.save(config);
        });

        refreshEnvironment(configs);
    }

    //  Reload DB configs into Spring Environment (runtime override)
    private void refreshEnvironment(Map<String, String> newConfigs) {
        Map<String, Object> dbConfigs = new HashMap<>();
        configRepository.findAll().forEach(c -> dbConfigs.put(c.getKeyName(), c.getValue()));

        // Add latest changes on top
        dbConfigs.putAll(newConfigs);

        // Replace "dbConfig" property source
        environment.getPropertySources().remove("dbConfig");
        environment.getPropertySources().addFirst(new MapPropertySource("dbConfig", dbConfigs));

        log.info("Spring Environment refreshed with latest DB configs: {}", newConfigs.keySet());
    }
}
