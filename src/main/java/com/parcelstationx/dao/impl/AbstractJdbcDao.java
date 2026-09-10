package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.BaseDao;
import com.parcelstationx.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractJdbcDao<T> implements BaseDao<T, Long> {
  protected final ConnectionProvider connections;
  private final String insertSql;
  private final String updateSql;
  private final String selectAllSql;
  private final String selectByIdSql;
  private final String deleteSql;
  private final SqlBinder<T> insertBinder;
  private final SqlBinder<T> updateBinder;
  private final RowMapper<T> mapper;

  protected AbstractJdbcDao(
      ConnectionProvider connections,
      String insertSql,
      String updateSql,
      String selectAllSql,
      String selectByIdSql,
      String deleteSql,
      SqlBinder<T> insertBinder,
      SqlBinder<T> updateBinder,
      RowMapper<T> mapper) {
    this.connections = connections;
    this.insertSql = insertSql;
    this.updateSql = updateSql;
    this.selectAllSql = selectAllSql;
    this.selectByIdSql = selectByIdSql;
    this.deleteSql = deleteSql;
    this.insertBinder = insertBinder;
    this.updateBinder = updateBinder;
    this.mapper = mapper;
  }

  protected abstract Long idOf(T entity);

  protected abstract T withId(T entity, long id);

  @Override
  public T save(T entity) {
    try (Connection connection = connections.getConnection()) {
      return save(connection, entity);
    } catch (SQLException exception) {
      throw failure("save", exception);
    }
  }

  public T save(Connection connection, T entity) {
    boolean insert = idOf(entity) == null;
    try (PreparedStatement statement =
        connection.prepareStatement(
            insert ? insertSql : updateSql,
            insert ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
      if (insert) {
        insertBinder.bind(statement, entity);
      } else {
        updateBinder.bind(statement, entity);
      }
      if (statement.executeUpdate() != 1) {
        throw new DatabaseException("Expected one affected row.", null);
      }
      if (!insert) {
        return entity;
      }
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (!keys.next()) {
          throw new DatabaseException("Database did not return a generated id.", null);
        }
        return withId(entity, keys.getLong(1));
      }
    } catch (SQLException exception) {
      throw failure("save", exception);
    }
  }

  public T restore(Connection connection, T entity) {
    if (idOf(entity) == null) return save(connection, entity);
    String withIdColumns = insertSql.replaceFirst("\\) VALUES", ",id) VALUES");
    int close = withIdColumns.lastIndexOf(')');
    String restoreSql = withIdColumns.substring(0, close) + ",?)";
    int parameterCount = (int) insertSql.chars().filter(character -> character == '?').count();
    try (PreparedStatement statement = connection.prepareStatement(restoreSql)) {
      insertBinder.bind(statement, entity);
      statement.setLong(parameterCount + 1, idOf(entity));
      if (statement.executeUpdate() != 1)
        throw new DatabaseException("Expected one restored row.", null);
      return entity;
    } catch (SQLException exception) {
      throw failure("restore", exception);
    }
  }

  @Override
  public Optional<T> findById(Long id) {
    return queryOne(selectByIdSql, (statement, ignored) -> statement.setLong(1, id));
  }

  public Optional<T> findById(Connection connection, Long id) {
    return queryOne(connection, selectByIdSql, (statement, ignored) -> statement.setLong(1, id));
  }

  @Override
  public List<T> findAll() {
    return query(selectAllSql, (statement, ignored) -> {});
  }

  public List<T> findAll(Connection connection) {
    return query(connection, selectAllSql, (statement, ignored) -> {});
  }

  @Override
  public boolean deleteById(Long id) {
    try (Connection connection = connections.getConnection();
        PreparedStatement statement = connection.prepareStatement(deleteSql)) {
      statement.setLong(1, id);
      return statement.executeUpdate() == 1;
    } catch (SQLException exception) {
      throw failure("delete", exception);
    }
  }

  protected Optional<T> queryOne(String sql, SqlBinder<Void> binder) {
    List<T> rows = query(sql, binder);
    return rows.stream().findFirst();
  }

  protected Optional<T> queryOne(Connection connection, String sql, SqlBinder<Void> binder) {
    return query(connection, sql, binder).stream().findFirst();
  }

  protected List<T> query(String sql, SqlBinder<Void> binder) {
    try (Connection connection = connections.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      binder.bind(statement, null);
      try (ResultSet resultSet = statement.executeQuery()) {
        List<T> rows = new ArrayList<>();
        while (resultSet.next()) {
          rows.add(mapper.map(resultSet));
        }
        return rows;
      }
    } catch (SQLException exception) {
      throw failure("query", exception);
    }
  }

  protected List<T> query(Connection connection, String sql, SqlBinder<Void> binder) {
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      binder.bind(statement, null);
      try (ResultSet resultSet = statement.executeQuery()) {
        List<T> rows = new ArrayList<>();
        while (resultSet.next()) {
          rows.add(mapper.map(resultSet));
        }
        return rows;
      }
    } catch (SQLException exception) {
      throw failure("query", exception);
    }
  }

  private DatabaseException failure(String operation, SQLException exception) {
    return new DatabaseException(
        getClass().getSimpleName() + " failed to " + operation + ".", exception);
  }
}
