package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.CustomerDao;
import com.parcelstationx.model.Customer;
import java.util.List;

public final class CustomerDaoImpl extends AbstractJdbcDao<Customer> implements CustomerDao {
  private static final String COLUMNS = "id,name,mobile,building,room,remark,created_at,updated_at";

  public CustomerDaoImpl(ConnectionProvider connections) {
    super(
        connections,
        "INSERT INTO customers(name,mobile,building,room,remark,created_at,updated_at) VALUES(?,?,?,?,?,?,?)",
        "UPDATE customers SET name=?,mobile=?,building=?,room=?,remark=?,created_at=?,updated_at=? WHERE id=?",
        "SELECT " + COLUMNS + " FROM customers ORDER BY id",
        "SELECT " + COLUMNS + " FROM customers WHERE id=?",
        "DELETE FROM customers WHERE id=?",
        CustomerDaoImpl::bindInsert,
        CustomerDaoImpl::bindUpdate,
        rows ->
            new Customer(
                rows.getLong("id"),
                rows.getString("name"),
                rows.getString("mobile"),
                rows.getString("building"),
                rows.getString("room"),
                rows.getString("remark"),
                JdbcValues.time(rows, "created_at"),
                JdbcValues.time(rows, "updated_at")));
  }

  @Override
  public List<Customer> findByMobile(String mobile) {
    return query(
        "SELECT " + COLUMNS + " FROM customers WHERE mobile LIKE ? ORDER BY id",
        (statement, ignored) -> statement.setString(1, "%" + mobile + "%"));
  }

  public List<Customer> findByMobile(java.sql.Connection connection, String mobile) {
    return query(
        connection,
        "SELECT " + COLUMNS + " FROM customers WHERE mobile=? ORDER BY id",
        (statement, ignored) -> statement.setString(1, mobile));
  }

  @Override
  protected Long idOf(Customer value) {
    return value.id();
  }

  @Override
  protected Customer withId(Customer v, long id) {
    return new Customer(
        id, v.name(), v.mobile(), v.building(), v.room(), v.remark(), v.createdAt(), v.updatedAt());
  }

  private static void bindInsert(java.sql.PreparedStatement s, Customer v)
      throws java.sql.SQLException {
    bind(s, v);
  }

  private static void bindUpdate(java.sql.PreparedStatement s, Customer v)
      throws java.sql.SQLException {
    bind(s, v);
    s.setLong(8, v.id());
  }

  private static void bind(java.sql.PreparedStatement s, Customer v) throws java.sql.SQLException {
    s.setString(1, v.name());
    s.setString(2, v.mobile());
    s.setString(3, v.building());
    s.setString(4, v.room());
    s.setString(5, v.remark());
    JdbcValues.time(s, 6, v.createdAt());
    JdbcValues.time(s, 7, v.updatedAt());
  }
}
