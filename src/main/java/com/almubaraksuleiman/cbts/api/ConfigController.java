package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.admin.model.SystemConfig;
import com.almubaraksuleiman.cbts.admin.repository.SystemConfigRepository;
import com.almubaraksuleiman.cbts.admin.service.SystemConfigService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 * */

@RestController
@RequestMapping("/api/admin/config")
@CrossOrigin(origins = "*") // allow frontend access (configure properly in prod)
public class ConfigController {

    private final SystemConfigService configService;
    private final SystemConfigRepository configRepository;

    public ConfigController(SystemConfigService configService, SystemConfigRepository configRepository) {
        this.configService = configService;
        this.configRepository = configRepository;
    }

    //  Get all configs
    @GetMapping
    public List<SystemConfig> getConfigs() {
        return configRepository.findAll();
    }

    // Update configs (bulk update from Angular form)
    @PostMapping
    public void saveConfigs(@RequestBody Map<String, String> configs) {
        configService.updateConfigs(configs);
    }

    // Get a single config
    @GetMapping("/{key}")
    public String getConfig(@PathVariable String key) {
        return configService.getConfig(key);
    }

    // Update a single config
    @PutMapping("/{key}")
    public void updateConfig(@PathVariable String key, @RequestBody String value) {
        configService.updateConfig(key, value);
    }
}
