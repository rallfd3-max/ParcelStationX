package com.parcelstationx.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.http.ApiServer;
import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.dao.UserDao;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelStatus;
import com.parcelstationx.model.User;
import com.parcelstationx.model.UserRole;
import com.parcelstationx.service.AuthenticationService;
import com.parcelstationx.service.PasswordHasher;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiServerTest {
  private final ObjectMapper json = new ObjectMapper();
  private final HttpClient client = HttpClient.newHttpClient();
  private ApiServer server;
  private String baseUrl;
  private FakeParcelDao parcels;

  @BeforeEach
  void startServer() throws Exception {
    PasswordHasher passwords = new PasswordHasher();
    LocalDateTime now = LocalDateTime.now();
    var users =
        new FakeUserDao(
            List.of(
                new User(
                    1L,
                    "admin",
                    passwords.hash("secret"),
                    "Administrator",
                    UserRole.ADMIN,
                    true,
                    now,
                    null),
                new User(
                    2L,
                    "staff",
                    passwords.hash("secret"),
                    "Staff",
                    UserRole.STAFF,
                    true,
                    now,
                    null)));
    parcels = new FakeParcelDao();
    parcels.items.add(
        new Parcel(
            10L,
            "TRACK-10",
            "SF",
            20L,
            30L,
            "123456",
            ParcelStatus.IN_STOCK,
            now,
            null,
            1L,
            "demo",
            now,
            now));
    server =
        new ApiServer(
            new InetSocketAddress("127.0.0.1", 0),
            new AuthenticationService(users, passwords),
            parcels,
            new SessionManager());
    server.start();
    baseUrl = "http://127.0.0.1:" + server.port();
  }

  @AfterEach
  void stopServer() {
    server.close();
  }

  @Test
  void healthIsPublic() throws Exception {
    HttpResponse<String> response = request("GET", "/api/health", null, null);
    assertEquals(200, response.statusCode());
    assertTrue(response.body().contains("\"status\":\"UP\""));
  }

  @Test
  void loginMeParcelQueryAndLogoutFormACompleteSessionFlow() throws Exception {
    HttpResponse<String> login = login("admin", "secret");
    assertEquals(200, login.statusCode());
    JsonNode loginJson = json.readTree(login.body());
    String token = loginJson.at("/data/token").asText();
    assertFalse(token.isBlank());
    assertEquals("ADMIN", loginJson.at("/data/user/role").asText());
    assertFalse(login.body().contains("passwordHash"));

    HttpResponse<String> me = request("GET", "/api/auth/me", null, token);
    assertEquals(200, me.statusCode());
    assertEquals("admin", json.readTree(me.body()).at("/data/username").asText());

    HttpResponse<String> list = request("GET", "/api/parcels", null, token);
    assertEquals(200, list.statusCode());
    assertEquals("TRACK-10", json.readTree(list.body()).at("/data/0/trackingNo").asText());
    assertFalse(list.body().contains("123456"));

    assertEquals(200, request("POST", "/api/auth/logout", "", token).statusCode());
    assertEquals(401, request("GET", "/api/auth/me", null, token).statusCode());
  }

  @Test
  void rejectsInvalidCredentialsWithoutSensitiveValues() throws Exception {
    HttpResponse<String> response = login("admin", "wrong-secret");
    assertEquals(401, response.statusCode());
    assertTrue(response.body().contains("INVALID_CREDENTIALS"));
    assertFalse(response.body().contains("wrong-secret"));
    assertFalse(response.body().contains("password_hash"));
  }

  @Test
  void requiresAuthenticationForParcelQueries() throws Exception {
    HttpResponse<String> response = request("GET", "/api/parcels", null, null);
    assertEquals(401, response.statusCode());
    assertTrue(response.body().contains("UNAUTHORIZED"));
  }

  @Test
  void enforcesAdminRoleOnServer() throws Exception {
    String token = json.readTree(login("staff", "secret").body()).at("/data/token").asText();
    HttpResponse<String> response = request("GET", "/api/admin/ping", null, token);
    assertEquals(403, response.statusCode());
    assertTrue(response.body().contains("FORBIDDEN"));
  }

  @Test
  void reportsMalformedJson() throws Exception {
    HttpResponse<String> response = request("POST", "/api/auth/login", "{broken", null);
    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("MALFORMED_JSON"));
  }

  @Test
  void distinguishesNotFoundAndMethodNotAllowed() throws Exception {
    assertEquals(404, request("GET", "/api/missing", null, null).statusCode());
    HttpResponse<String> response = request("PUT", "/api/health", "", null);
    assertEquals(405, response.statusCode());
    assertTrue(response.body().contains("METHOD_NOT_ALLOWED"));
  }

  @Test
  void returnsParcelDetailAndTypedMissingErrors() throws Exception {
    String token = json.readTree(login("admin", "secret").body()).at("/data/token").asText();
    assertEquals(200, request("GET", "/api/parcels/10", null, token).statusCode());
    assertEquals(404, request("GET", "/api/parcels/999", null, token).statusCode());
    assertEquals(400, request("GET", "/api/parcels/not-a-number", null, token).statusCode());
  }

  @Test
  void internalErrorsDoNotLeakStackOrDatabaseDetails() throws Exception {
    String token = json.readTree(login("admin", "secret").body()).at("/data/token").asText();
    parcels.fail = true;
    HttpResponse<String> response = request("GET", "/api/parcels", null, token);
    assertEquals(500, response.statusCode());
    assertTrue(response.body().contains("INTERNAL_ERROR"));
    assertFalse(response.body().contains("SQLException"));
    assertFalse(response.body().contains("secret-db-password"));
  }

  private HttpResponse<String> login(String username, String password) throws Exception {
    return request(
        "POST",
        "/api/auth/login",
        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
        null);
  }

  private HttpResponse<String> request(String method, String path, String body, String token)
      throws Exception {
    HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path));
    if (token != null) {
      builder.header("Authorization", "Bearer " + token);
    }
    if (body == null) {
      builder.method(method, HttpRequest.BodyPublishers.noBody());
    } else {
      builder
          .header("Content-Type", "application/json")
          .method(method, HttpRequest.BodyPublishers.ofString(body));
    }
    return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static final class FakeUserDao implements UserDao {
    private final List<User> users;

    private FakeUserDao(List<User> users) {
      this.users = users;
    }

    @Override
    public Optional<User> findByUsername(String username) {
      return users.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    @Override
    public User save(User entity) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<User> findById(Long id) {
      return users.stream().filter(user -> user.id().equals(id)).findFirst();
    }

    @Override
    public List<User> findAll() {
      return List.copyOf(users);
    }

    @Override
    public boolean deleteById(Long id) {
      throw new UnsupportedOperationException();
    }
  }

  private static final class FakeParcelDao implements ParcelDao {
    private final List<Parcel> items = new ArrayList<>();
    private boolean fail;

    @Override
    public Optional<Parcel> findByTrackingNo(String trackingNo) {
      return items.stream().filter(parcel -> parcel.trackingNo().equals(trackingNo)).findFirst();
    }

    @Override
    public Optional<Parcel> findByPickupCode(String pickupCode) {
      return items.stream().filter(parcel -> parcel.pickupCode().equals(pickupCode)).findFirst();
    }

    @Override
    public Parcel save(Parcel entity) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Parcel> findById(Long id) {
      return items.stream().filter(parcel -> parcel.id().equals(id)).findFirst();
    }

    @Override
    public List<Parcel> findAll() {
      if (fail) {
        throw new IllegalStateException("SQLException at jdbc:mysql://db/secret-db-password");
      }
      return List.copyOf(items);
    }

    @Override
    public boolean deleteById(Long id) {
      throw new UnsupportedOperationException();
    }
  }
}
