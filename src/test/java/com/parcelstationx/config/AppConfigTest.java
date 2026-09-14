package com.parcelstationx.config;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void notificationConfigurationSortsStagesAndRejectsUnsupportedRealSms() {
    NotificationConfig config =
        NotificationConfig.load(
            new Properties(), Map.of("PARCEL_OVERDUE_REMINDER_DAYS", "15,7,10,7"));
    assertEquals(java.util.List.of(7, 10, 15), config.overdueDays());
    assertEquals("mock", config.smsMode());
    assertThrows(
        ConfigurationException.class,
        () -> NotificationConfig.load(new Properties(), Map.of("PARCEL_SMS_MODE", "real")));
  }
}
