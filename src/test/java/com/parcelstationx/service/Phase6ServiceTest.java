package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.impl.*;
import com.parcelstationx.model.*;
import com.parcelstationx.task.NotificationQueue;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Phase6ServiceTest {
  private ConnectionProvider connections;
  private ParcelDaoImpl parcels;
  private CustomerDaoImpl customers;
  private ExceptionRecordDaoImpl exceptions;
  private ParcelEventDaoImpl events;
  private OperationLogDaoImpl logs;

  @BeforeEach
  void setup() throws Exception {
    String url = "jdbc:h2:mem:p6" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
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
          "CREATE TABLE parcels(id BIGINT AUTO_INCREMENT PRIMARY KEY,tracking_no VARCHAR(100),courier_company VARCHAR(50),customer_id BIGINT,shelf_id BIGINT,pickup_code VARCHAR(20),status VARCHAR(30),arrived_at TIMESTAMP,picked_up_at TIMESTAMP,operator_id BIGINT,remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE exception_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,exception_type VARCHAR(30),description VARCHAR(500),status VARCHAR(20),created_by BIGINT,handled_by BIGINT,created_at TIMESTAMP,handled_at TIMESTAMP,resolution VARCHAR(500))");
      s.execute(
          "CREATE TABLE parcel_events(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,event_type VARCHAR(30),from_status VARCHAR(30),to_status VARCHAR(30),operator_id BIGINT,description VARCHAR(255),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE operation_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,operation_type VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,description VARCHAR(500),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE notification_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,customer_id BIGINT,notification_type VARCHAR(30),target VARCHAR(100),content VARCHAR(500),status VARCHAR(20),retry_count INT,created_at TIMESTAMP,sent_at TIMESTAMP,error_message VARCHAR(255))");
    }
    customers = new CustomerDaoImpl(connections);
    parcels = new ParcelDaoImpl(connections);
    exceptions = new ExceptionRecordDaoImpl(connections);
    events = new ParcelEventDaoImpl(connections);
    logs = new OperationLogDaoImpl(connections);
    LocalDateTime now = LocalDateTime.now();
    customers.save(new Customer(null, "A", "13800000000", null, null, null, now, now));
    parcels.save(
        new Parcel(
            null,
            "T1",
            "SF",
            1L,
            1L,
            "123456",
            ParcelStatus.IN_STOCK,
            now,
            null,
            1L,
            null,
            now,
            now));
  }

  @Test
  void exceptionFlowPersistsStateEventsAndLogs() {
    ExceptionService service =
        new ExceptionService(new TransactionRunner(connections), exceptions, parcels, events, logs);
    ExceptionRecord record = service.create(1L, ExceptionType.DAMAGED, "broken", 1L);
    assertEquals(ParcelStatus.EXCEPTION, parcels.findById(1L).orElseThrow().status());
    service.resolve(record.id(), ParcelStatus.IN_STOCK, "fixed", 1L);
    assertEquals(ParcelStatus.IN_STOCK, parcels.findById(1L).orElseThrow().status());
    assertEquals(2, events.findAll().size());
    assertEquals(2, logs.findAll().size());
  }

  @Test
  void notificationPersistsSuccess() {
    NotificationRecordDaoImpl records = new NotificationRecordDaoImpl(connections);
    try (NotificationService service =
        new NotificationService(records, customers, new NotificationQueue())) {
      service.notifyInbound(parcels.findById(1L).orElseThrow()).join();
    }
    assertEquals(NotificationStatus.SUCCESS, records.findAll().get(0).status());
  }
}
