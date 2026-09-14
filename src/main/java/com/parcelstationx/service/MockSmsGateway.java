package com.parcelstationx.service;

import com.parcelstationx.model.NotificationRecord;

public final class MockSmsGateway implements NotificationGateway {
  @Override
  public void send(NotificationRecord record) {
    System.out.println(
        "[SMS MOCK] simulated delivery to "
            + mask(record.target())
            + " type="
            + record.notificationType());
  }

  static String mask(String mobile) {
    if (mobile == null || mobile.length() < 7) return "****";
    return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
  }
}
