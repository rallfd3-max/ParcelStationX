package com.parcelstationx.config;

import com.parcelstationx.exception.ConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public final class AppConfig {
  private static final String LOCAL_PROPERTIES = "application.properties";

  private AppConfig() {}

  public static DatabaseConfig loadDatabaseConfig() {
    Properties properties = new Properties();
    try (InputStream input =
        AppConfig.class.getClassLoader().getResourceAsStream(LOCAL_PROPERTIES)) {
      if (input != null) {
        properties.load(input);
      }
    } catch (IOException exception) {
      throw new ConfigurationException("Unable to read local database configuration.", exception);
    }

    return loadDatabaseConfig(properties, System.getenv());
  }

  static DatabaseConfig loadDatabaseConfig(Properties properties, Map<String, String> environment) {
    String url = value("PARCEL_DB_URL", properties, "db.url", environment);
    String username = value("PARCEL_DB_USERNAME", properties, "db.username", environment);
    String password = value("PARCEL_DB_PASSWORD", properties, "db.password", environment);
    if (isBlank(url) || isBlank(username) || password == null) {
      throw new ConfigurationException(
          "Database configuration is missing. Copy application.example.properties to application.properties or set PARCEL_DB_* variables.");
    }
    return new DatabaseConfig(url, username, password);
  }

  private static String value(
      String environmentKey,
      Properties properties,
      String propertyKey,
      Map<String, String> environment) {
    String environmentValue = environment.get(environmentKey);
    return isBlank(environmentValue) ? properties.getProperty(propertyKey) : environmentValue;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
