package com.parcelstationx.service;

import com.parcelstationx.model.NotificationRecord;

@FunctionalInterface
public interface NotificationGateway {
  void send(NotificationRecord record);
}
