package com.parcelstationx.api;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.http.ApiServer;
import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.impl.*;
import com.parcelstationx.model.*;
import com.parcelstationx.service.*;
import java.net.*;
import java.net.http.*;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;

class ManagementApiTest {
  private final ObjectMapper json = new ObjectMapper();
  private final HttpClient client = HttpClient.newHttpClient();
  private ApiServer server;
  private String base;

  @BeforeEach
  void start() throws Exception {
    String url = "jdbc:h2:mem:api" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    ConnectionProvider cp =
        () -> {
          try {
            return DriverManager.getConnection(url);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        };
    try (var c = cp.getConnection();
        var s = c.createStatement()) {
      s.execute(
          "CREATE TABLE users(id BIGINT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(50),password_hash VARCHAR(255),display_name VARCHAR(50),role VARCHAR(20),enabled BOOLEAN,created_at TIMESTAMP,last_login_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE customers(id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(50),mobile VARCHAR(20),building VARCHAR(50),room VARCHAR(50),remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE shelves(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_code VARCHAR(50),zone_name VARCHAR(50),capacity INT,occupied INT,status VARCHAR(20),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE shelf_layout(shelf_id BIGINT PRIMARY KEY,position_x DOUBLE,position_y DOUBLE,position_z DOUBLE,rotation_y DOUBLE,width DOUBLE,height DOUBLE,depth DOUBLE,columns_count INT,levels_count INT,updated_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE shelf_slots(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_id BIGINT,slot_code VARCHAR(50),level_index INT,column_index INT,enabled BOOLEAN,created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE parcels(id BIGINT AUTO_INCREMENT PRIMARY KEY,tracking_no VARCHAR(100),courier_company VARCHAR(50),customer_id BIGINT,shelf_id BIGINT,pickup_code VARCHAR(20),status VARCHAR(30),arrived_at TIMESTAMP,picked_up_at TIMESTAMP,operator_id BIGINT,remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP,slot_id BIGINT UNIQUE,version BIGINT DEFAULT 0)");
      s.execute(
          "CREATE TABLE parcel_events(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,event_type VARCHAR(30),from_status VARCHAR(30),to_status VARCHAR(30),operator_id BIGINT,description VARCHAR(255),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE parcel_relocations(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,from_slot_id BIGINT,new_slot_id BIGINT,operator_id BIGINT,reason VARCHAR(255),created_at TIMESTAMP)");
      s.execute(
          "CREATE TABLE exception_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,exception_type VARCHAR(30),description VARCHAR(500),status VARCHAR(20),created_by BIGINT,handled_by BIGINT,created_at TIMESTAMP,handled_at TIMESTAMP,resolution VARCHAR(500))");
      s.execute(
          "CREATE TABLE operation_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,operation_type VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,description VARCHAR(500),created_at TIMESTAMP)");
    }
    var passwords = new PasswordHasher();
    var now = LocalDateTime.now();
    var users = new UserDaoImpl(cp);
    users.save(
        new User(
            null, "admin", passwords.hash("secret"), "Admin", UserRole.ADMIN, true, now, null));
    users.save(
        new User(
            null, "staff", passwords.hash("secret"), "Staff", UserRole.STAFF, true, now, null));
    var customers = new CustomerDaoImpl(cp);
    customers.save(new Customer(null, "Customer", "13800000000", null, null, null, now, now));
    var shelves = new ShelfDaoImpl(cp);
    shelves.save(new Shelf(null, "A01", "A", 4, 1, ShelfStatus.ACTIVE, now));
    var layouts = new ShelfLayoutDaoImpl(cp);
    layouts.save(new ShelfLayout(1L, 0, 0, 0, 0, 4, 2, 1, 2, 2, now));
    var slots = new ShelfSlotDaoImpl(cp);
    slots.save(new ShelfSlot(null, 1L, "A01-01-01", 1, 1, true, now));
    var parcels = new ParcelDaoImpl(cp);
    parcels.save(
        new Parcel(
            null,
            "TRACK-MGMT",
            "SF",
            1L,
            1L,
            "654321",
            ParcelStatus.IN_STOCK,
            now,
            null,
            1L,
            "",
            now,
            now,
            1L,
            0));
    var events = new ParcelEventDaoImpl(cp);
    var relocations = new ParcelRelocationDaoImpl(cp);
    var logs = new OperationLogDaoImpl(cp);
    var tx = new TransactionRunner(cp);
    var warehouse = new WarehouseLayoutService(shelves, layouts, slots, parcels);
    server =
        new ApiServer(
            new InetSocketAddress("127.0.0.1", 0),
            new AuthenticationService(users, passwords),
            parcels,
            new SessionManager(),
            warehouse,
            new RelocationService(tx, parcels, slots, shelves, relocations, events, logs),
            relocations,
            new ParcelQueryService(parcels, customers, users, shelves, slots, events, relocations),
            new ExceptionService(tx, new ExceptionRecordDaoImpl(cp), parcels, events, logs),
            new UserService(users, passwords));
    server.start();
    base = "http://127.0.0.1:" + server.port();
  }

  @AfterEach
  void stop() {
    if (server != null) server.close();
  }

  @Test
  void parcelDetailsEventsAndRelocationsAreAvailable() throws Exception {
    String token = login("admin");
    JsonNode detail = data(request("GET", "/api/parcels/1/details", null, token));
    assertEquals("TRACK-MGMT", detail.at("/parcel/trackingNo").asText());
    assertEquals("138****0000", detail.at("/customer/maskedMobile").asText());
    assertFalse(detail.toString().contains("passwordHash"));
    assertEquals(200, request("GET", "/api/parcels/1/events", null, token).statusCode());
    assertEquals(200, request("GET", "/api/parcels/1/relocations", null, token).statusCode());
  }

  @Test
  void exceptionCreateListAndResolveUseBusinessService() throws Exception {
    String token = login("staff");
    assertEquals(
        200,
        request(
                "POST",
                "/api/parcels/1/exception",
                "{\"exceptionType\":\"DAMAGED\",\"description\":\"broken\"}",
                token)
            .statusCode());
    JsonNode list = data(request("GET", "/api/exceptions", null, token));
    long id = list.get(0).get("id").asLong();
    assertEquals(
        200,
        request(
                "POST",
                "/api/exceptions/" + id + "/resolve",
                "{\"targetStatus\":\"IN_STOCK\",\"resolution\":\"repacked\"}",
                token)
            .statusCode());
    assertEquals(
        "IN_STOCK", data(request("GET", "/api/parcels/1", null, token)).get("status").asText());
  }

  @Test
  void adminEndpointsRejectStaffAndNeverExposeHashes() throws Exception {
    assertEquals(403, request("GET", "/api/admin/users", null, login("staff")).statusCode());
    HttpResponse<String> users = request("GET", "/api/admin/users", null, login("admin"));
    assertEquals(200, users.statusCode());
    assertFalse(users.body().contains("passwordHash"));
  }

  @Test
  void adminCanCreateUserAndUpdateLayoutWhileOccupiedSlotCannotBeDisabled() throws Exception {
    String token = login("admin");
    assertEquals(
        200,
        request(
                "POST",
                "/api/admin/users",
                "{\"username\":\"newstaff\",\"password\":\"secret2\",\"displayName\":\"New\",\"role\":\"STAFF\"}",
                token)
            .statusCode());
    String layout =
        "{\"shelfId\":1,\"positionX\":3,\"positionY\":0,\"positionZ\":2,\"rotationY\":0,\"width\":4,\"height\":2,\"depth\":1,\"columns\":2,\"levels\":2}";
    assertEquals(200, request("PUT", "/api/admin/layouts/1", layout, token).statusCode());
    assertEquals(
        400,
        request("PUT", "/api/admin/slots/1/enabled", "{\"enabled\":false}", token).statusCode());
  }

  private String login(String user) throws Exception {
    return json.readTree(
            request(
                    "POST",
                    "/api/auth/login",
                    "{\"username\":\"" + user + "\",\"password\":\"secret\"}",
                    null)
                .body())
        .at("/data/token")
        .asText();
  }

  private JsonNode data(HttpResponse<String> response) throws Exception {
    assertEquals(200, response.statusCode(), response.body());
    return json.readTree(response.body()).get("data");
  }

  private HttpResponse<String> request(String method, String path, String body, String token)
      throws Exception {
    var b = HttpRequest.newBuilder(URI.create(base + path));
    if (token != null) b.header("Authorization", "Bearer " + token);
    if (body == null) b.method(method, HttpRequest.BodyPublishers.noBody());
    else
      b.header("Content-Type", "application/json")
          .method(method, HttpRequest.BodyPublishers.ofString(body));
    return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }
}
