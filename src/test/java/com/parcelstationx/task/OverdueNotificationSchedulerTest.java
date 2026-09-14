package com.parcelstationx.task;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.ai.*;
import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.config.NotificationConfig;
import com.parcelstationx.dao.impl.CustomerDaoImpl;
import com.parcelstationx.dao.impl.NotificationRecordDaoImpl;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.model.*;
import com.parcelstationx.service.*;
import java.sql.DriverManager;
import java.time.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OverdueNotificationSchedulerTest {
  private ParcelDaoImpl parcels;
  private CustomerDaoImpl customers;
  private NotificationRecordDaoImpl records;
  private LocalDateTime arrived;

  @BeforeEach
  void setup() throws Exception {
    String url = "jdbc:h2:mem:notify" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    ConnectionProvider connections =
        () -> {
          try {
            return DriverManager.getConnection(url);
          } catch (Exception exception) {
            throw new IllegalStateException(exception);
          }
        };
    try (var connection = connections.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE customers(id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(50),mobile VARCHAR(20),building VARCHAR(50),room VARCHAR(50),remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE parcels(id BIGINT AUTO_INCREMENT PRIMARY KEY,tracking_no VARCHAR(100),courier_company VARCHAR(50),customer_id BIGINT,shelf_id BIGINT,pickup_code VARCHAR(20),status VARCHAR(30),arrived_at TIMESTAMP,picked_up_at TIMESTAMP,operator_id BIGINT,remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP,slot_id BIGINT UNIQUE,version BIGINT DEFAULT 0)");
      statement.execute(
          "CREATE TABLE notification_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,customer_id BIGINT,notification_type VARCHAR(30),target VARCHAR(100),content VARCHAR(500),status VARCHAR(20),retry_count INT,created_at TIMESTAMP,sent_at TIMESTAMP,error_message VARCHAR(255))");
    }
    parcels = new ParcelDaoImpl(connections);
    customers = new CustomerDaoImpl(connections);
    records = new NotificationRecordDaoImpl(connections);
    arrived = LocalDateTime.of(2026, 1, 1, 8, 0);
    customers.save(new Customer(null, "客户", "13900000001", null, null, null, arrived, arrived));
    parcels.save(parcel(null, ParcelStatus.IN_STOCK));
  }

  @Test
  void sendsStagesOnceAcrossRepeatedRunsAndRestarts() {
    runAt(6);
    assertTrue(records.findAll().isEmpty());
    runAt(7);
    assertTypes("OVERDUE_DAY_7");
    runAt(8);
    runAt(9);
    runAt(10);
    runAt(10);
    runAt(15);
    assertTypes("OVERDUE_DAY_15", "OVERDUE_DAY_10", "OVERDUE_DAY_7");
    assertTrue(records.findAll().stream().allMatch(r -> r.status() == NotificationStatus.SUCCESS));
  }

  @Test
  void pickedUpParcelNeverReceivesReminderAndCloseTerminatesExecutor() {
    parcels.save(parcel(1L, ParcelStatus.PICKED_UP));
    var service = service(record -> {});
    var scheduler = scheduler(service, 15);
    scheduler.runOnce();
    scheduler.close();
    service.close();
    assertTrue(records.findAll().isEmpty());
    assertTrue(scheduler.isShutdown());
  }

  @Test
  void gatewayFailurePersistsFailedAndRetryCanSucceed() {
    AtomicInteger attempts = new AtomicInteger();
    NotificationService service =
        service(
            record -> {
              if (attempts.getAndIncrement() == 0)
                throw new IllegalStateException("secret provider detail");
            });
    service.notifyInbound(parcels.findById(1L).orElseThrow()).join();
    NotificationRecord failed = records.findAll().get(0);
    assertEquals(NotificationStatus.FAILED, failed.status());
    assertEquals("IllegalStateException", failed.errorMessage());
    service.retry(failed.id()).join();
    service.close();
    assertEquals(NotificationStatus.SUCCESS, records.findAll().get(0).status());
    assertEquals(1, records.findAll().get(0).retryCount());
  }

  @Test
  void invalidAiCopyFallsBackWithoutSendingRealValuesToModel() {
    FakeAiClient ai = new FakeAiClient("missing placeholders");
    NotificationContentService content = new NotificationContentService(ai);
    Parcel parcel = parcels.findById(1L).orElseThrow();
    String message = content.inbound(parcel);
    assertTrue(message.contains(parcel.pickupCode()));
    assertFalse(ai.lastUserPrompt().contains(parcel.pickupCode()));
    assertFalse(ai.lastUserPrompt().contains("13900000001"));
  }

  private void runAt(int day) {
    NotificationService service = service(record -> {});
    OverdueNotificationScheduler scheduler = scheduler(service, day);
    scheduler.runOnce();
    scheduler.close();
    service.close();
  }

  private NotificationService service(NotificationGateway gateway) {
    return new NotificationService(
        records,
        customers,
        new NotificationQueue(),
        gateway,
        parcels,
        new NotificationContentService(null),
        3);
  }

  private OverdueNotificationScheduler scheduler(NotificationService service, int day) {
    Clock clock =
        Clock.fixed(
            arrived.plusDays(day).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
    return new OverdueNotificationScheduler(
        parcels, service, new NotificationConfig(List.of(7, 10, 15), 60, 3, "mock"), clock);
  }

  private Parcel parcel(Long id, ParcelStatus status) {
    return new Parcel(
        id,
        "DEMO-NOTIFY",
        "顺丰",
        1L,
        null,
        "654321",
        status,
        arrived,
        status == ParcelStatus.PICKED_UP ? arrived.plusDays(2) : null,
        1L,
        "",
        arrived,
        arrived,
        null,
        0);
  }

  private void assertTypes(String... types) {
    assertEquals(
        List.of(types),
        records.findAll().stream().map(NotificationRecord::notificationType).toList());
  }
}
