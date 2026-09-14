package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.impl.*;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.*;
import com.parcelstationx.task.NotificationQueue;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParcelServiceTest {
  private ConnectionProvider connections;
  private ParcelService service;
  private ShelfDaoImpl shelves;
  private ParcelEventDaoImpl events;
  private OperationLogDaoImpl logs;

  @BeforeEach
  void setup() throws Exception {
    String url = "jdbc:h2:mem:service" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    connections =
        () -> {
          try {
            return DriverManager.getConnection(url);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        };
    try (var c = connections.getConnection();
        var s = c.createStatement()) {
      s.execute(
          "CREATE TABLE customers(id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(50),mobile VARCHAR(20),building VARCHAR(50),room VARCHAR(50),remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE shelves(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_code VARCHAR(30),zone_name VARCHAR(30),capacity INT,occupied INT,status VARCHAR(20),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE parcels(id BIGINT AUTO_INCREMENT PRIMARY KEY,tracking_no VARCHAR(100) UNIQUE,courier_company VARCHAR(50),customer_id BIGINT,shelf_id BIGINT,pickup_code VARCHAR(20),status VARCHAR(30),arrived_at TIMESTAMP,picked_up_at TIMESTAMP,operator_id BIGINT,remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP,slot_id BIGINT UNIQUE,version BIGINT DEFAULT 0)");
      s.execute(
          "CREATE TABLE parcel_events(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,event_type VARCHAR(30),from_status VARCHAR(30),to_status VARCHAR(30),operator_id BIGINT,description VARCHAR(255),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE operation_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,operation_type VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,description VARCHAR(500),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE notification_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,customer_id BIGINT,notification_type VARCHAR(30),target VARCHAR(100),content VARCHAR(500),status VARCHAR(20),retry_count INT,created_at TIMESTAMP,sent_at TIMESTAMP,error_message VARCHAR(255))");
    }
    CustomerDaoImpl customers = new CustomerDaoImpl(connections);
    shelves = new ShelfDaoImpl(connections);
    ParcelDaoImpl parcels = new ParcelDaoImpl(connections);
    events = new ParcelEventDaoImpl(connections);
    logs = new OperationLogDaoImpl(connections);
    service =
        new ParcelService(
            new TransactionRunner(connections), customers, shelves, parcels, events, logs);
    LocalDateTime now = LocalDateTime.now();
    customers.save(new Customer(null, "Alice", "13800000000", "1", "101", "", now, now));
    shelves.save(new Shelf(null, "A-1", "A", 1, 0, ShelfStatus.ACTIVE, now));
  }

  @Test
  void inboundAndOutboundUpdateAllRecords() {
    Parcel stored =
        service.inbound(new InboundRequest("TRACK-001", "SF", "13800000000", null, 1L, ""));
    assertNull(stored.slotId());
    assertNull(stored.shelfId());
    assertEquals(0, shelves.findById(1L).orElseThrow().occupied());
    assertEquals(1, events.findByParcelId(stored.id()).size());
    assertEquals(1, logs.findAll().size());
    Parcel picked = service.outbound(stored.pickupCode(), 1L);
    assertEquals(ParcelStatus.PICKED_UP, picked.status());
    assertNotNull(picked.pickedUpAt());
    assertEquals(0, shelves.findById(1L).orElseThrow().occupied());
    assertEquals(2, events.findByParcelId(stored.id()).size());
    assertEquals(2, logs.findAll().size());
  }

  @Test
  void rejectsDuplicateDisabledShelfInvalidAndDuplicatePickup() {
    Parcel stored =
        service.inbound(new InboundRequest("TRACK-002", "SF", "13800000000", null, 1L, ""));
    assertThrows(
        BusinessException.class,
        () -> service.inbound(new InboundRequest("TRACK-002", "SF", "13800000000", null, 1L, "")));
    Shelf shelf = shelves.findById(1L).orElseThrow();
    shelves.save(
        new Shelf(
            shelf.id(),
            shelf.shelfCode(),
            shelf.zone(),
            shelf.capacity(),
            shelf.occupied(),
            ShelfStatus.DISABLED,
            shelf.createdAt()));
    assertThrows(
        BusinessException.class,
        () -> service.inbound(new InboundRequest("TRACK-003", "SF", "13800000000", 1L, 1L, "")));
    assertThrows(BusinessException.class, () -> service.outbound("bad", 1L));
    service.outbound(stored.pickupCode(), 1L);
    assertThrows(BusinessException.class, () -> service.outbound(stored.pickupCode(), 1L));
  }

  @Test
  void rollsBackInboundWhenAuditWriteFails() throws Exception {
    NotificationRecordDaoImpl notificationRecords = new NotificationRecordDaoImpl(connections);
    NotificationService notificationService =
        new NotificationService(
            notificationRecords, new CustomerDaoImpl(connections), new NotificationQueue());
    service.withNotifications(notificationService);
    try (var connection = connections.getConnection();
        var statement = connection.createStatement()) {
      statement.execute("DROP TABLE operation_logs");
    }
    assertThrows(
        RuntimeException.class,
        () ->
            service.inbound(
                new InboundRequest("TRACK-ROLLBACK", "SF", "13800000000", null, 1L, "")));
    try (var connection = connections.getConnection();
        var statement = connection.createStatement()) {
      try (var rows = statement.executeQuery("SELECT COUNT(*) FROM parcels")) {
        rows.next();
        assertEquals(0, rows.getInt(1));
      }
      try (var rows = statement.executeQuery("SELECT occupied FROM shelves WHERE id=1")) {
        rows.next();
        assertEquals(0, rows.getInt(1));
      }
    }
    notificationService.close();
    assertTrue(notificationRecords.findAll().isEmpty());
  }
}
