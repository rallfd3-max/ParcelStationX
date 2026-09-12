package com.parcelstationx.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.config.DatabaseConfig;
import com.parcelstationx.dao.impl.*;
import com.parcelstationx.model.*;
import com.parcelstationx.service.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MySqlWorkflowIT {
  private ConnectionFactory factory;
  private CustomerDaoImpl customers;
  private ShelfDaoImpl shelves;
  private ParcelDaoImpl parcels;
  private ParcelEventDaoImpl events;
  private OperationLogDaoImpl logs;
  private ParcelService service;
  private Long testCustomerId;
  private Long testParcelId;
  private String testTracking;

  @BeforeEach
  void setup() {
    String url = System.getenv("PARCEL_DB_URL"),
        user = System.getenv("PARCEL_DB_USERNAME"),
        password = System.getenv("PARCEL_DB_PASSWORD");
    assumeTrue(url != null && user != null && password != null);
    factory = new ConnectionFactory(new DatabaseConfig(url, user, password));
    customers = new CustomerDaoImpl(factory);
    shelves = new ShelfDaoImpl(factory);
    parcels = new ParcelDaoImpl(factory);
    events = new ParcelEventDaoImpl(factory);
    logs = new OperationLogDaoImpl(factory);
    service =
        new ParcelService(
            new TransactionRunner(factory), customers, shelves, parcels, events, logs);
  }

  @AfterEach
  void cleanup() throws Exception {
    if (factory == null) return;
    try (var connection = factory.getConnection()) {
      connection.setAutoCommit(false);
      try (var statement =
          connection.prepareStatement("DELETE FROM notification_records WHERE parcel_id=?")) {
        if (testParcelId != null) {
          statement.setLong(1, testParcelId);
          statement.executeUpdate();
        }
      }
      try (var statement =
          connection.prepareStatement("DELETE FROM exception_records WHERE parcel_id=?")) {
        if (testParcelId != null) {
          statement.setLong(1, testParcelId);
          statement.executeUpdate();
        }
      }
      try (var statement =
          connection.prepareStatement("DELETE FROM parcel_events WHERE parcel_id=?")) {
        if (testParcelId != null) {
          statement.setLong(1, testParcelId);
          statement.executeUpdate();
        }
      }
      try (var statement =
          connection.prepareStatement("DELETE FROM operation_logs WHERE target_id=?")) {
        if (testParcelId != null) {
          statement.setLong(1, testParcelId);
          statement.executeUpdate();
        }
      }
      try (var statement = connection.prepareStatement("DELETE FROM parcels WHERE id=?")) {
        if (testParcelId != null) {
          statement.setLong(1, testParcelId);
          statement.executeUpdate();
        }
      }
      try (var statement = connection.prepareStatement("DELETE FROM customers WHERE id=?")) {
        if (testCustomerId != null) {
          statement.setLong(1, testCustomerId);
          statement.executeUpdate();
        }
      }
      connection.commit();
    }
  }

  @Test
  void loginCustomerInboundOutboundAndEvents() {
    User user =
        new AuthenticationService(new UserDaoImpl(factory), new PasswordHasher())
            .login("admin", "admin123".toCharArray());
    LocalDateTime now = LocalDateTime.now();
    Customer customer =
        customers.save(
            new Customer(null, "IT Customer", "13899990000", "IT", "1", "integration", now, now));
    testCustomerId = customer.id();
    Shelf shelf = shelves.findAvailable().orElseThrow();
    int before = shelf.occupied();
    Parcel parcel =
        service.inbound(
            new InboundRequest(
                "IT-" + System.nanoTime(),
                "SF",
                customer.mobile(),
                shelf.id(),
                user.id(),
                "integration"));
    testParcelId = parcel.id();
    testTracking = parcel.trackingNo();
    assertThrows(
        com.parcelstationx.exception.BusinessException.class,
        () ->
            service.inbound(
                new InboundRequest(
                    testTracking, "SF", customer.mobile(), shelf.id(), user.id(), "duplicate")));
    assertEquals(before + 1, shelves.findById(shelf.id()).orElseThrow().occupied());
    assertTrue(
        events.findByParcelId(parcel.id()).stream().anyMatch(e -> e.eventType().equals("STORED")));
    assertTrue(logs.findAll().stream().anyMatch(l -> l.targetId().equals(parcel.id())));
    service.outbound(parcel.pickupCode(), user.id());
    assertEquals(before, shelves.findById(shelf.id()).orElseThrow().occupied());
    assertEquals(ParcelStatus.PICKED_UP, parcels.findById(parcel.id()).orElseThrow().status());
  }
}
