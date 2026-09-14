package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.dao.impl.ShelfLayoutDaoImpl;
import com.parcelstationx.dao.impl.ShelfSlotDaoImpl;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.exception.DatabaseException;
import com.parcelstationx.model.Shelf;
import com.parcelstationx.model.ShelfSlot;
import com.parcelstationx.model.ShelfStatus;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShelfManagementServiceTest {
  private ShelfDaoImpl shelves;
  private ShelfLayoutDaoImpl layouts;
  private ShelfSlotDaoImpl slots;
  private OperationLogDaoImpl logs;
  private ShelfManagementService service;

  @BeforeEach
  void setUp() throws Exception {
    String url = "jdbc:h2:mem:shelves" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    ConnectionProvider connections =
        () -> {
          try {
            return DriverManager.getConnection(url);
          } catch (java.sql.SQLException exception) {
            throw new IllegalStateException(exception);
          }
        };
    try (var connection = connections.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE shelves(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_code VARCHAR(30) NOT NULL UNIQUE,zone_name VARCHAR(30) NOT NULL,capacity INT NOT NULL,occupied INT NOT NULL,status VARCHAR(20) NOT NULL,created_at TIMESTAMP NOT NULL)");
      statement.execute(
          "CREATE TABLE shelf_layout(shelf_id BIGINT PRIMARY KEY,position_x DOUBLE NOT NULL,position_y DOUBLE NOT NULL,position_z DOUBLE NOT NULL,rotation_y DOUBLE NOT NULL,width DOUBLE NOT NULL,height DOUBLE NOT NULL,depth DOUBLE NOT NULL,columns_count INT NOT NULL,levels_count INT NOT NULL,updated_at TIMESTAMP NOT NULL)");
      statement.execute(
          "CREATE TABLE shelf_slots(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_id BIGINT NOT NULL,slot_code VARCHAR(50) NOT NULL UNIQUE,level_index INT NOT NULL,column_index INT NOT NULL,enabled BOOLEAN NOT NULL,created_at TIMESTAMP NOT NULL,UNIQUE(shelf_id,level_index,column_index))");
      statement.execute(
          "CREATE TABLE operation_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,operation_type VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,description VARCHAR(500),created_at TIMESTAMP)");
    }
    shelves = new ShelfDaoImpl(connections);
    layouts = new ShelfLayoutDaoImpl(connections);
    slots = new ShelfSlotDaoImpl(connections);
    logs = new OperationLogDaoImpl(connections);
    service =
        new ShelfManagementService(
            new TransactionRunner(connections), shelves, layouts, slots, logs);
  }

  @Test
  void createsOneShelfLayoutAndThirtySlots() {
    ShelfCreationResult result = service.create(request(null, "E区", 1, 5, 6), 7L);

    assertEquals(1, result.shelfCount());
    assertEquals(30, result.slotCount());
    assertEquals("E-01", result.shelves().get(0).shelf().shelfCode());
    assertEquals(1, shelves.findAll().size());
    assertEquals(1, layouts.findAll().size());
    assertEquals(30, slots.findAll().size());
    assertEquals("E-01-05-06", slots.findAll().get(29).slotCode());
    assertEquals("SHELF_CREATE", logs.findAll().get(0).operationType());
  }

  @Test
  void createsFourShelvesAndOneHundredTwentyUniqueSlots() {
    ShelfCreationResult result = service.create(request(null, "E", 4, 5, 6), 7L);

    assertEquals(4, result.shelfCount());
    assertEquals(120, result.slotCount());
    assertEquals(120, slots.findAll().stream().map(ShelfSlot::slotCode).distinct().count());
    assertEquals("E-04", result.shelves().get(3).shelf().shelfCode());
    assertEquals("SHELF_BATCH_CREATE", logs.findAll().get(0).operationType());
  }

  @Test
  void previewDoesNotWriteAndExplicitDuplicateIsRejected() {
    ShelfCreationResult preview = service.preview(request(null, "F", 2, 3, 4));
    assertTrue(preview.preview());
    assertTrue(shelves.findAll().isEmpty());

    service.create(request("F-01", "F", 1, 3, 4), 7L);
    assertThrows(BusinessException.class, () -> service.create(request("F-01", "F", 1, 3, 4), 7L));
    assertEquals(1, shelves.findAll().size());
  }

  @Test
  void validationRejectsInvalidCountsGridAndDimensions() {
    assertThrows(BusinessException.class, () -> service.preview(request(null, "E", 0, 5, 6)));
    assertThrows(BusinessException.class, () -> service.preview(request(null, "E", 1, 0, 6)));
    assertThrows(
        BusinessException.class,
        () -> service.preview(new ShelfCreationRequest(null, "E", 1, 5, 6, -1, 2.6, .8)));
  }

  @Test
  void middleFailureRollsBackTheWholeBatch() {
    var now = LocalDateTime.now();
    Shelf holder = shelves.save(new Shelf(null, "A-01", "A", 1, 0, ShelfStatus.ACTIVE, now));
    slots.save(new ShelfSlot(null, holder.id(), "E-02-01-01", 1, 1, true, now));

    assertThrows(DatabaseException.class, () -> service.create(request(null, "E", 2, 1, 1), 7L));
    assertEquals(1, shelves.findAll().size());
    assertTrue(layouts.findAll().isEmpty());
    assertEquals(1, slots.findAll().size());
    assertTrue(logs.findAll().isEmpty());
  }

  private ShelfCreationRequest request(
      String code, String zone, int count, int levels, int columns) {
    return new ShelfCreationRequest(code, zone, count, levels, columns, 3.6, 2.6, .8);
  }
}
