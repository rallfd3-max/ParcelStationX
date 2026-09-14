package com.parcelstationx.config;

import com.parcelstationx.exception.ConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public record NotificationConfig(
    List<Integer> overdueDays, int scanMinutes, int maxRetries, String smsMode) {
  public static NotificationConfig load(Properties properties, Map<String, String> environment) {
    String stages =
        value(
            properties,
            environment,
            "notification.overdue-days",
            "PARCEL_OVERDUE_REMINDER_DAYS",
            "7,10,15");
    List<Integer> days;
    try {
      days =
          Arrays.stream(stages.split(","))
              .map(String::trim)
              .map(Integer::parseInt)
              .filter(n -> n > 0 && n <= 365)
              .distinct()
              .sorted()
              .toList();
    } catch (NumberFormatException exception) {
      throw new ConfigurationException("Overdue reminder days must be comma-separated integers.");
    }
    if (days.isEmpty())
      throw new ConfigurationException("At least one overdue reminder day is required.");
    int scan =
        integer(
            value(
                properties,
                environment,
                "notification.scan-minutes",
                "PARCEL_NOTIFICATION_SCAN_MINUTES",
                "60"),
            "scan minutes");
    int retries =
        integer(
            value(
                properties,
                environment,
                "notification.max-retries",
                "PARCEL_NOTIFICATION_MAX_RETRIES",
                "3"),
            "max retries");
    String smsMode =
        value(properties, environment, "notification.sms-mode", "PARCEL_SMS_MODE", "mock");
    if (!smsMode.equalsIgnoreCase("mock")) {
      throw new ConfigurationException(
          "Only PARCEL_SMS_MODE=mock is supported until a real provider contract is configured.");
    }
    return new NotificationConfig(days, scan, retries, "mock");
  }

  private static String value(
      Properties properties,
      Map<String, String> environment,
      String property,
      String env,
      String fallback) {
    String value = environment.get(env);
    if (value == null || value.isBlank()) value = properties.getProperty(property, fallback);
    return value.trim();
  }

  private static int integer(String value, String label) {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed <= 0) throw new NumberFormatException();
      return parsed;
    } catch (NumberFormatException exception) {
      throw new ConfigurationException("Notification " + label + " must be a positive integer.");
    }
  }
}
