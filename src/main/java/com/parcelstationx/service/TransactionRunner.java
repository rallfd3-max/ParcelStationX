package com.parcelstationx.service;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.exception.DatabaseException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public final class TransactionRunner {
  private final ConnectionProvider connections;

  public TransactionRunner(ConnectionProvider connections) {
    this.connections = connections;
  }

  public <T> T run(Function<Connection, T> action) {
    try (Connection connection = connections.getConnection()) {
      connection.setAutoCommit(false);
      try {
        T result = action.apply(connection);
        connection.commit();
        return result;
      } catch (RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    } catch (SQLException exception) {
      throw new DatabaseException("Transaction failed and was rolled back.", exception);
    }
  }
}
