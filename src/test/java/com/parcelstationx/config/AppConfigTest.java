package com.parcelstationx.config;

import com.parcelstationx.exception.ConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest {
    @Test
    void failsClearlyWhenNoLocalDatabaseConfigurationExists() {
        assertThrows(ConfigurationException.class, AppConfig::loadDatabaseConfig);
    }
}
