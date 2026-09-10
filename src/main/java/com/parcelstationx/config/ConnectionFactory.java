package com.parcelstationx.config;

import com.parcelstationx.exception.DatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory implements ConnectionProvider {
  private final DatabaseConfig databaseConfig;

  public ConnectionFactory(DatabaseConfig databaseConfig) {
    this.databaseConfig = databaseConfig;
  }

  public Connection getConnection() {
    try {
      return DriverManager.getConnection(
          databaseConfig.url(), databaseConfig.username(), databaseConfig.password());
    } catch (SQLException exception) {
      throw new DatabaseException(
          "Unable to connect to MySQL. Check database configuration and service status.",
          exception);
    }
  }
}
