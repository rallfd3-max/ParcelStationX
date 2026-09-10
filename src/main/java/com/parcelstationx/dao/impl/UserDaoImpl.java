package com.parcelstationx.dao.impl;

import com.parcelstationx.config.ConnectionProvider;
import com.parcelstationx.dao.UserDao;
import com.parcelstationx.model.User;
import com.parcelstationx.model.UserRole;
import java.util.Optional;

public final class UserDaoImpl extends AbstractJdbcDao<User> implements UserDao {
  private static final String COLUMNS =
      "id,username,password_hash,display_name,role,enabled,created_at,last_login_at";

  public UserDaoImpl(ConnectionProvider c) {
    super(
        c,
        "INSERT INTO users(username,password_hash,display_name,role,enabled,created_at,last_login_at) VALUES(?,?,?,?,?,?,?)",
        "UPDATE users SET username=?,password_hash=?,display_name=?,role=?,enabled=?,created_at=?,last_login_at=? WHERE id=?",
        "SELECT " + COLUMNS + " FROM users ORDER BY id",
        "SELECT " + COLUMNS + " FROM users WHERE id=?",
        "DELETE FROM users WHERE id=?",
        UserDaoImpl::bind,
        (s, v) -> {
          bind(s, v);
          s.setLong(8, v.id());
        },
        r ->
            new User(
                r.getLong("id"),
                r.getString("username"),
                r.getString("password_hash"),
                r.getString("display_name"),
                UserRole.valueOf(r.getString("role")),
                r.getBoolean("enabled"),
                JdbcValues.time(r, "created_at"),
                JdbcValues.time(r, "last_login_at")));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return queryOne(
        "SELECT " + COLUMNS + " FROM users WHERE username=?", (s, v) -> s.setString(1, username));
  }

  @Override
  protected Long idOf(User v) {
    return v.id();
  }

  @Override
  protected User withId(User v, long id) {
    return new User(
        id,
        v.username(),
        v.passwordHash(),
        v.displayName(),
        v.role(),
        v.enabled(),
        v.createdAt(),
        v.lastLoginAt());
  }

  private static void bind(java.sql.PreparedStatement s, User v) throws java.sql.SQLException {
    s.setString(1, v.username());
    s.setString(2, v.passwordHash());
    s.setString(3, v.displayName());
    s.setString(4, v.role().name());
    s.setBoolean(5, v.enabled());
    JdbcValues.time(s, 6, v.createdAt());
    JdbcValues.time(s, 7, v.lastLoginAt());
  }
}
