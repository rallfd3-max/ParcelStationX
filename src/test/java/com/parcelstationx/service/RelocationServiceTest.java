package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.error.ConflictException;
import com.parcelstationx.api.error.UnprocessableException;
import com.parcelstationx.api.http.ApiServer;
import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.UserDao;
import com.parcelstationx.dao.impl.*;
import com.parcelstationx.exception.DatabaseException;
import com.parcelstationx.model.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RelocationServiceTest {
  private ConnectionProvider connections;
  private ParcelDaoImpl parcels;
  private ShelfDaoImpl shelves;
  private ShelfSlotDaoImpl slots;
  private ParcelRelocationDaoImpl relocations;
  private ParcelEventDaoImpl events;
  private OperationLogDaoImpl logs;
  private RelocationService service;
  private LocalDateTime now;

  @BeforeEach
  void setUp() throws Exception {
    String url = "jdbc:h2:mem:relocate" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    connections =
        () -> {
          try {
            return DriverManager.getConnection(url);
          } catch (Exception exception) {
            throw new RuntimeException(exception);
          }
        };
    try (var connection = connections.getConnection();
        var statement = connection.createStatement()) {
      statement.execute("CREATE TABLE users(id BIGINT PRIMARY KEY)");
      statement.execute("CREATE TABLE customers(id BIGINT PRIMARY KEY)");
      statement.execute(
          "CREATE TABLE shelves(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_code VARCHAR(30) UNIQUE,zone_name VARCHAR(30),capacity INT,occupied INT,status VARCHAR(20),created_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE shelf_layout(shelf_id BIGINT PRIMARY KEY,position_x DECIMAL,position_y DECIMAL,position_z DECIMAL,rotation_y DECIMAL,width DECIMAL,height DECIMAL,depth DECIMAL,columns_count INT,levels_count INT,updated_at TIMESTAMP,FOREIGN KEY(shelf_id) REFERENCES shelves(id))");
      statement.execute(
          "CREATE TABLE shelf_slots(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_id BIGINT,slot_code VARCHAR(50) UNIQUE,level_index INT,column_index INT,enabled BOOLEAN,created_at TIMESTAMP,UNIQUE(shelf_id,level_index,column_index),FOREIGN KEY(shelf_id) REFERENCES shelves(id))");
      statement.execute(
          "CREATE TABLE parcels(id BIGINT AUTO_INCREMENT PRIMARY KEY,tracking_no VARCHAR(100) UNIQUE,courier_company VARCHAR(50),customer_id BIGINT,shelf_id BIGINT,pickup_code VARCHAR(20),status VARCHAR(30),arrived_at TIMESTAMP,picked_up_at TIMESTAMP,operator_id BIGINT,remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP,slot_id BIGINT UNIQUE,version BIGINT DEFAULT 0,FOREIGN KEY(slot_id) REFERENCES shelf_slots(id))");
      statement.execute(
          "CREATE TABLE parcel_relocations(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,from_slot_id BIGINT,new_slot_id BIGINT,operator_id BIGINT,reason VARCHAR(255),created_at TIMESTAMP,FOREIGN KEY(parcel_id) REFERENCES parcels(id),FOREIGN KEY(operator_id) REFERENCES users(id))");
      statement.execute(
          "CREATE TABLE parcel_events(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,event_type VARCHAR(30),from_status VARCHAR(30),to_status VARCHAR(30),operator_id BIGINT,description VARCHAR(255),created_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE operation_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,operation_type VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,description VARCHAR(500),created_at TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(id))");
      statement.execute("INSERT INTO users(id) VALUES(1)");
      statement.execute("INSERT INTO customers(id) VALUES(1)");
    }
    parcels = new ParcelDaoImpl(connections);
    shelves = new ShelfDaoImpl(connections);
    slots = new ShelfSlotDaoImpl(connections);
    relocations = new ParcelRelocationDaoImpl(connections);
    events = new ParcelEventDaoImpl(connections);
    logs = new OperationLogDaoImpl(connections);
    service =
        new RelocationService(
            new TransactionRunner(connections), parcels, slots, shelves, relocations, events, logs);
    now = LocalDateTime.now().withNano(0);
  }

  @Test
  void createsAndQueriesLayoutAndSlotsWithUniqueGrid() {
    Shelf shelf = shelf("A-01", 2);
    ShelfLayoutDaoImpl layouts = new ShelfLayoutDaoImpl(connections);
    ShelfLayout layout = new ShelfLayout(shelf.id(), 1, 0, 2, 0.5, 3, 2, 1, 2, 1, now);
    assertEquals(layout, layouts.save(layout));
    assertEquals(1, layouts.findAll().size());
    assertEquals(1, slot(shelf.id(), "A-01-01", 0, 0).id());
    assertThrows(DatabaseException.class, () -> slot(shelf.id(), "A-01-02", 0, 0));
  }

  @Test
  void relocatesWaitingParcelAndPersistsAllAuditRecords() {
    Shelf shelf = shelf("A-01", 2);
    ShelfSlot target = slot(shelf.id(), "A-01-01", 0, 0);
    Parcel parcel = parcel("TRACK-1", ParcelStatus.IN_STOCK, null, null, 0);

    RelocationResult result = service.relocate(parcel.id(), target.id(), 0, "2d drag", 1);

    assertEquals(target.id(), result.parcel().slotId());
    assertEquals(1, result.parcel().version());
    assertEquals(target.id(), parcels.findById(parcel.id()).orElseThrow().slotId());
    assertEquals(1, shelves.findById(shelf.id()).orElseThrow().occupied());
    assertEquals(1, relocations.findByParcelId(parcel.id()).size());
    assertEquals("RELOCATED", events.findByParcelId(parcel.id()).get(0).eventType());
    assertEquals("RELOCATE", logs.findAll().get(0).operationType());
  }

  @Test
  void rejectsOccupiedSlotAndStaleVersionWithoutPartialWrites() {
    Shelf shelf = shelf("A-01", 3);
    ShelfSlot first = slot(shelf.id(), "A-01-01", 0, 0);
    ShelfSlot second = slot(shelf.id(), "A-01-02", 0, 1);
    Parcel one = parcel("TRACK-1", ParcelStatus.IN_STOCK, shelf.id(), first.id(), 0);
    Parcel two = parcel("TRACK-2", ParcelStatus.IN_STOCK, null, null, 0);

    ConflictException occupied =
        assertThrows(
            ConflictException.class, () -> service.relocate(two.id(), first.id(), 0, "move", 1));
    assertEquals("SLOT_OCCUPIED", occupied.code());
    ConflictException stale =
        assertThrows(
            ConflictException.class, () -> service.relocate(one.id(), second.id(), 9, "move", 1));
    assertEquals("PARCEL_VERSION_CONFLICT", stale.code());
    assertNull(parcels.findById(two.id()).orElseThrow().slotId());
    assertTrue(relocations.findAll().isEmpty());
  }

  @Test
  void rejectsInvalidParcelState() {
    Shelf shelf = shelf("A-01", 2);
    ShelfSlot target = slot(shelf.id(), "A-01-01", 0, 0);
    Parcel parcel = parcel("TRACK-1", ParcelStatus.PICKED_UP, null, null, 0);
    assertThrows(
        UnprocessableException.class,
        () -> service.relocate(parcel.id(), target.id(), 0, "move", 1));
  }

  @Test
  void databaseFailureRollsBackParcelShelfEventAndLog() {
    Shelf shelf = shelf("A-01", 2);
    ShelfSlot target = slot(shelf.id(), "A-01-01", 0, 0);
    Parcel parcel = parcel("TRACK-1", ParcelStatus.IN_STOCK, null, null, 0);

    assertThrows(
        DatabaseException.class, () -> service.relocate(parcel.id(), target.id(), 0, "move", 999));

    Parcel stored = parcels.findById(parcel.id()).orElseThrow();
    assertNull(stored.slotId());
    assertEquals(0, stored.version());
    assertEquals(0, shelves.findById(shelf.id()).orElseThrow().occupied());
    assertTrue(relocations.findAll().isEmpty());
    assertTrue(events.findAll().isEmpty());
    assertTrue(logs.findAll().isEmpty());
  }

  @Test
  void warehouseAndRelocateApisUseTheTransactionService() throws Exception {
    Shelf shelf = shelf("A-01", 2);
    ShelfLayoutDaoImpl layouts = new ShelfLayoutDaoImpl(connections);
    layouts.save(new ShelfLayout(shelf.id(), 0, 0, 0, 0, 2, 2, 1, 2, 1, now));
    ShelfSlot target = slot(shelf.id(), "A-01-01", 0, 0);
    Parcel parcel = parcel("TRACK-API", ParcelStatus.IN_STOCK, null, null, 0);
    PasswordHasher hasher = new PasswordHasher();
    User admin =
        new User(1L, "admin", hasher.hash("secret"), "Admin", UserRole.ADMIN, true, now, null);
    try (ApiServer api =
        new ApiServer(
            new InetSocketAddress("127.0.0.1", 0),
            new AuthenticationService(new SingleUserDao(admin), hasher),
            parcels,
            new SessionManager(),
            new WarehouseLayoutService(shelves, layouts, slots, parcels),
            service,
            relocations)) {
      api.start();
      HttpClient client = HttpClient.newHttpClient();
      String base = "http://127.0.0.1:" + api.port();
      HttpRequest loginRequest =
          HttpRequest.newBuilder(URI.create(base + "/api/auth/login"))
              .POST(
                  HttpRequest.BodyPublishers.ofString(
                      "{\"username\":\"admin\",\"password\":\"secret\"}"))
              .build();
      String login = client.send(loginRequest, HttpResponse.BodyHandlers.ofString()).body();
      String token =
          new com.fasterxml.jackson.databind.ObjectMapper()
              .readTree(login)
              .at("/data/token")
              .asText();
      HttpRequest warehouse =
          HttpRequest.newBuilder(URI.create(base + "/api/warehouse"))
              .header("Authorization", "Bearer " + token)
              .GET()
              .build();
      assertTrue(
          client.send(warehouse, HttpResponse.BodyHandlers.ofString()).body().contains("A-01-01"));
      HttpRequest relocate =
          HttpRequest.newBuilder(URI.create(base + "/api/parcels/" + parcel.id() + "/relocate"))
              .header("Authorization", "Bearer " + token)
              .POST(
                  HttpRequest.BodyPublishers.ofString(
                      "{\"targetSlotId\":"
                          + target.id()
                          + ",\"expectedVersion\":0,\"reason\":\"api\"}"))
              .build();
      HttpResponse<String> response = client.send(relocate, HttpResponse.BodyHandlers.ofString());
      assertEquals(200, response.statusCode());
      assertTrue(response.body().contains("\"version\":1"));
      HttpResponse<String> conflict = client.send(relocate, HttpResponse.BodyHandlers.ofString());
      assertEquals(409, conflict.statusCode());
      assertTrue(conflict.body().contains("PARCEL_VERSION_CONFLICT"));
    }
  }

  private Shelf shelf(String code, int capacity) {
    return shelves.save(new Shelf(null, code, "Z", capacity, 0, ShelfStatus.ACTIVE, now));
  }

  private ShelfSlot slot(long shelfId, String code, int level, int column) {
    return slots.save(new ShelfSlot(null, shelfId, code, level, column, true, now));
  }

  private Parcel parcel(
      String tracking, ParcelStatus status, Long shelfId, Long slotId, long version) {
    return parcels.save(
        new Parcel(
            null, tracking, "SF", 1L, shelfId, "123456", status, now, null, 1L, "", now, now,
            slotId, version));
  }

  private record SingleUserDao(User user) implements UserDao {
    @Override
    public Optional<User> findByUsername(String username) {
      return user.username().equals(username) ? Optional.of(user) : Optional.empty();
    }

    @Override
    public User save(User value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<User> findById(Long id) {
      return user.id().equals(id) ? Optional.of(user) : Optional.empty();
    }

    @Override
    public List<User> findAll() {
      return List.of(user);
    }

    @Override
    public boolean deleteById(Long id) {
      throw new UnsupportedOperationException();
    }
  }
}
