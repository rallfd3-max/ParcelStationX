package com.parcelstationx.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.impl.*;
import com.parcelstationx.model.*;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcDaoCrudTest {
  private ConnectionProvider connections;

  @BeforeEach
  void createSchema() throws Exception {
    String url = "jdbc:h2:mem:dao" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    connections =
        () -> {
          try {
            return DriverManager.getConnection(url);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        };
    try (var connection = connections.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE customers(id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(50),mobile VARCHAR(20),building VARCHAR(50),room VARCHAR(50),remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE users(id BIGINT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(50),password_hash VARCHAR(255),display_name VARCHAR(50),role VARCHAR(20),enabled BOOLEAN,created_at TIMESTAMP,last_login_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE shelves(id BIGINT AUTO_INCREMENT PRIMARY KEY,shelf_code VARCHAR(30),zone_name VARCHAR(30),capacity INT,occupied INT,status VARCHAR(20),created_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE parcels(id BIGINT AUTO_INCREMENT PRIMARY KEY,tracking_no VARCHAR(100),courier_company VARCHAR(50),customer_id BIGINT,shelf_id BIGINT,pickup_code VARCHAR(20),status VARCHAR(30),arrived_at TIMESTAMP,picked_up_at TIMESTAMP,operator_id BIGINT,remark VARCHAR(255),created_at TIMESTAMP,updated_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE parcel_events(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,event_type VARCHAR(30),from_status VARCHAR(30),to_status VARCHAR(30),operator_id BIGINT,description VARCHAR(255),created_at TIMESTAMP)");
      statement.execute(
          "CREATE TABLE exception_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,exception_type VARCHAR(30),description VARCHAR(500),status VARCHAR(20),created_by BIGINT,handled_by BIGINT,created_at TIMESTAMP,handled_at TIMESTAMP,resolution VARCHAR(500))");
      statement.execute(
          "CREATE TABLE notification_records(id BIGINT AUTO_INCREMENT PRIMARY KEY,parcel_id BIGINT,customer_id BIGINT,notification_type VARCHAR(30),target VARCHAR(100),content VARCHAR(500),status VARCHAR(20),retry_count INT,created_at TIMESTAMP,sent_at TIMESTAMP,error_message VARCHAR(255))");
      statement.execute(
          "CREATE TABLE operation_logs(id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,operation_type VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,description VARCHAR(500),created_at TIMESTAMP)");
    }
  }

  @Test
  void customerCrudUsesJdbc() {
    CustomerDao dao = new CustomerDaoImpl(connections);
    LocalDateTime now = LocalDateTime.now().withNano(0);
    Customer saved = dao.save(new Customer(null, "张三", "13800000000", "1", "101", "", now, now));
    assertNotNull(saved.id());
    assertEquals("张三", dao.findById(saved.id()).orElseThrow().name());
    assertEquals(1, dao.findByMobile("138").size());
    dao.save(new Customer(saved.id(), "张三改", "13800000000", "1", "101", "", now, now));
    assertEquals("张三改", dao.findById(saved.id()).orElseThrow().name());
    assertTrue(dao.deleteById(saved.id()));
    assertTrue(dao.findAll().isEmpty());
    try (var connection = connections.getConnection()) {
      ((CustomerDaoImpl) dao).restore(connection, saved);
    } catch (Exception exception) {
      fail(exception);
    }
    assertEquals(saved.id(), dao.findById(saved.id()).orElseThrow().id());
  }

  @Test
  void allEntityDaosPerformCrud() {
    LocalDateTime now = LocalDateTime.now().withNano(0);
    assertCrud(
        new UserDaoImpl(connections),
        new User(null, "u", "h", "U", UserRole.ADMIN, true, now, null));
    assertCrud(
        new ShelfDaoImpl(connections), new Shelf(null, "A-1", "A", 10, 0, ShelfStatus.ACTIVE, now));
    assertCrud(
        new ParcelDaoImpl(connections),
        new Parcel(
            null,
            "TRACK01",
            "SF",
            1L,
            1L,
            "123456",
            ParcelStatus.IN_STOCK,
            now,
            null,
            1L,
            "",
            now,
            now));
    assertCrud(
        new ParcelEventDaoImpl(connections),
        new ParcelEvent(null, 1L, "STORED", null, ParcelStatus.IN_STOCK, 1L, "", now));
    assertCrud(
        new ExceptionRecordDaoImpl(connections),
        new ExceptionRecord(
            null, 1L, ExceptionType.DAMAGED, "broken", "OPEN", 1L, null, now, null, null));
    assertCrud(
        new NotificationRecordDaoImpl(connections),
        new NotificationRecord(
            null,
            1L,
            1L,
            "INBOUND",
            "13800000000",
            "ready",
            NotificationStatus.PENDING,
            0,
            now,
            null,
            null));
    assertCrud(
        new OperationLogDaoImpl(connections),
        new OperationLog(null, 1L, "TEST", "PARCEL", 1L, "", now));
  }

  private <T> void assertCrud(BaseDao<T, Long> dao, T value) {
    T saved = dao.save(value);
    assertNotNull(saved);
    assertEquals(1, dao.findAll().size());
    assertTrue(dao.findById(1L).isPresent());
    assertTrue(dao.deleteById(1L));
  }
}
