package com.parcelstationx.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MySqlConnectionIT {
  @Test
  void connectsAndFindsRequiredTables() throws Exception {
    String url = System.getenv("PARCEL_DB_URL"),
        user = System.getenv("PARCEL_DB_USERNAME"),
        password = System.getenv("PARCEL_DB_PASSWORD");
    assumeTrue(url != null && user != null && password != null, "PARCEL_DB_* is not configured");
    try (var connection = DriverManager.getConnection(url, user, password);
        var statement =
            connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?,?,?,?,?,?,?,?,?,?)")) {
      String[] tables = {
        "users",
        "customers",
        "shelves",
        "parcels",
        "parcel_events",
        "exception_records",
        "notification_records",
        "operation_logs",
        "shelf_layout",
        "shelf_slots",
        "parcel_relocations"
      };
      for (int i = 0; i < tables.length; i++) statement.setString(i + 1, tables[i]);
      try (var rows = statement.executeQuery()) {
        rows.next();
        assertTrue(rows.getInt(1) == 11, "V2 schema/migration has not created all tables");
      }
    }
    try (var connection = DriverManager.getConnection(url, user, password);
        var count =
            connection.prepareStatement(
                "SELECT (SELECT COUNT(*) FROM users),(SELECT COUNT(*) FROM customers),(SELECT COUNT(*) FROM parcels),(SELECT COUNT(*) FROM exception_records),(SELECT COUNT(*) FROM operation_logs)")) {
      try (var rows = count.executeQuery()) {
        rows.next();
        assertTrue(rows.getInt(1) >= 3);
        assertTrue(rows.getInt(2) >= 20);
        assertTrue(rows.getInt(3) >= 50);
        assertTrue(rows.getInt(4) >= 10);
        assertTrue(rows.getInt(5) >= 30);
      }
      try (var rows =
          connection
              .createStatement()
              .executeQuery(
                  "SELECT s.id,s.shelf_code,s.occupied,COUNT(p.id) FROM shelves s LEFT JOIN shelf_slots ss ON ss.shelf_id=s.id LEFT JOIN parcels p ON p.slot_id=ss.id AND p.status IN ('IN_STOCK','EXCEPTION') GROUP BY s.id,s.shelf_code,s.occupied ORDER BY s.id")) {
        int index = 0;
        Set<String> codes = new HashSet<>();
        while (rows.next()) {
          codes.add(rows.getString(2));
          assertEquals(rows.getInt(4), rows.getInt(3));
          index++;
        }
        assertTrue(index >= 8, "V2.1 baseline shelves must remain after dynamic shelf creation");
        assertTrue(
            codes.containsAll(
                Set.of("A-01", "A-02", "B-01", "B-02", "C-01", "C-02", "D-01", "D-02")));
      }
      try (var rows =
          connection.createStatement().executeQuery("SELECT COUNT(*) FROM shelf_slots")) {
        rows.next();
        assertTrue(rows.getInt(1) >= 240, "V2.1 migration must expose at least 240 real slots");
      }
      try (var rows =
          connection
              .createStatement()
              .executeQuery(
                  "SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND constraint_type IN ('UNIQUE','FOREIGN KEY','CHECK')")) {
        rows.next();
        assertTrue(rows.getInt(1) > 0);
      }
    }
  }
}
