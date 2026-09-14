package com.parcelstationx.ai;

public record AiIntentRequest(AiIntent intent, Filters filters) {
  public record Filters(String trackingNo, String courier, String zone, int days, int limit) {}
}
