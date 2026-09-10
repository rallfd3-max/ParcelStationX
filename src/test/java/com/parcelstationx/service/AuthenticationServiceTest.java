package com.parcelstationx.service;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.impl.UserDaoImpl;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.User;
import com.parcelstationx.model.UserRole;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {
  @Test
  void authenticatesAgainstUsersTable() throws Exception {
    String url = "jdbc:h2:mem:auth;MODE=MySQL;DB_CLOSE_DELAY=-1";
    ConnectionProvider connections =
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
          "CREATE TABLE users(id BIGINT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(50),password_hash VARCHAR(255),display_name VARCHAR(50),role VARCHAR(20),enabled BOOLEAN,created_at TIMESTAMP,last_login_at TIMESTAMP)");
    }
    PasswordHasher hasher = new PasswordHasher();
    UserDaoImpl users = new UserDaoImpl(connections);
    users.save(
        new User(
            null,
            "admin",
            hasher.hash("admin123"),
            "管理员",
            UserRole.ADMIN,
            true,
            LocalDateTime.now(),
            null));
    AuthenticationService service = new AuthenticationService(users, hasher);
    assertEquals("admin", service.login("admin", "admin123".toCharArray()).username());
    assertThrows(BusinessException.class, () -> service.login("admin", "bad".toCharArray()));
  }
}
