package com.parcelstationx.service;

import com.parcelstationx.ai.AiClient;
import com.parcelstationx.model.Parcel;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NotificationContentService {
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([A-Z_]+)}}");
  private final AiClient ai;

  public NotificationContentService(AiClient ai) {
    this.ai = ai;
  }

  public String inbound(Parcel parcel) {
    String fallback = "【ParcelStationX】您的{{COURIER}}快件已到达驿站，取件码为{{PICKUP_CODE}}，请您方便时前来领取。";
    return replace(
        template("inbound", fallback, Set.of("COURIER", "PICKUP_CODE")), parcel, 0, "驿站");
  }

  public String overdue(Parcel parcel, int days) {
    String fallback = "【ParcelStationX】您的{{COURIER}}快件已在{{LOCATION}}存放{{DAYS}}天，目前仍未领取，请您近期前来领取。";
    return replace(
        template("overdue", fallback, Set.of("COURIER", "LOCATION", "DAYS")), parcel, days, "驿站");
  }

  private String template(String kind, String fallback, Set<String> required) {
    if (ai == null) return fallback;
    try {
      String result =
          ai.generate(
              "Generate one concise Chinese SMS template for "
                  + kind
                  + ". Keep required placeholders exactly. Do not output real phone numbers or pickup codes.",
              "Required placeholders: " + required);
      if (result == null || result.isBlank() || result.length() > 300) return fallback;
      Matcher matcher = PLACEHOLDER.matcher(result);
      java.util.Set<String> found = new java.util.HashSet<>();
      while (matcher.find()) found.add(matcher.group(1));
      if (!found.equals(required)) return fallback;
      return result;
    } catch (RuntimeException failure) {
      return fallback;
    }
  }

  private static String replace(String template, Parcel parcel, int days, String location) {
    return template
        .replace("{{COURIER}}", parcel.courierCompany())
        .replace("{{PICKUP_CODE}}", parcel.pickupCode())
        .replace("{{DAYS}}", String.valueOf(days))
        .replace("{{LOCATION}}", location);
  }
}
