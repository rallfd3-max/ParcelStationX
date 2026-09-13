package com.parcelstationx.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parcelstationx.exception.ConfigurationException;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class AppConfigTest {
  @Test
  void failsClearlyWhenNoLocalDatabaseConfigurationExists() {
    assertThrows(
        ConfigurationException.class,
        () -> AppConfig.loadDatabaseConfig(new Properties(), Map.of()));
  }
}
