package com.parcelstationx.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parcelstationx.exception.ConfigurationException;
import org.junit.jupiter.api.Test;

class AppConfigTest {
  @Test
  void failsClearlyWhenNoLocalDatabaseConfigurationExists() {
    assertThrows(ConfigurationException.class, AppConfig::loadDatabaseConfig);
  }
}
