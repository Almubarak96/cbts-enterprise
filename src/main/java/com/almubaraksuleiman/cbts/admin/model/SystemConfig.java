package com.almubaraksuleiman.cbts.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 * */

@Data
@Entity
@Table(name = "app_config")
@AllArgsConstructor

public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyName;


    @Column(nullable = false, length = 5000)
    private String value;

    //public SystemConfig() {}

    public SystemConfig(String keyName, String value) {
        this.keyName = keyName;
        this.value = value;
    }

}
