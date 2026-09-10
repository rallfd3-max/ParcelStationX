package com.parcelstationx.task;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class NotificationQueueTest {
  @Test
  void runsTaskAndCloses() {
    AtomicBoolean called = new AtomicBoolean();
    try (NotificationQueue queue = new NotificationQueue()) {
      queue.submit(() -> called.set(true)).join();
    }
    assertTrue(called.get());
  }
}
